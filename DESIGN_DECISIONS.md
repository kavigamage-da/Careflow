# Design Decisions

This document explains three of the more consequential technical decisions behind CareFlow, why they were made, what they cost, and what would change if the project moved beyond a local demonstration environment. It's written for anyone reviewing the codebase — a recruiter, an interviewer, or a future contributor — who wants the reasoning, not just the result.

---

## 1. Offline-First with Room, Instead of a Backend

### The decision

CareFlow stores all data locally in a Room (SQLite) database on-device. There is no server, no network calls, and no remote persistence layer in the current implementation.

### Why

A hospital workflow app is fundamentally a *state machine with a lot of actors* — reception, nurses, doctors, lab techs, pharmacists, cashiers, and admins all reading and writing overlapping data (patients, queue tickets, encounters, prescriptions, invoices). The interesting engineering problem in a project like this is **modeling that shared state correctly and safely** — foreign keys, transactional updates, concurrency-aware queue changes, role-based data access. None of that requires a network to demonstrate.

Building a real backend (auth server, REST API, sync protocol, conflict resolution) would have added a large amount of infrastructure work that doesn't showcase Android-specific skills, and would have shifted the project from "demonstrate mobile architecture and business-rule modeling" into "build and host a distributed system" — a different (and much larger) project. Room let the same domain logic — repositories, ViewModels, state flows — be exercised fully, tested locally, and reasoned about without also debugging network latency, auth token refresh, or server infrastructure.

### The trade-off

This is a real limitation, not a cosmetic one, and it's called out directly in the README's "Current Scope & Limitations" section rather than glossed over. Specifically:

- **No multi-device sync.** A receptionist's tablet and a doctor's tablet don't share state in real time — this only works as a single-device demo, or with manual coordination across devices.
- **No true concurrency across users.** The "concurrency-aware queue updates" mentioned in the README apply within a single local database (e.g. two ViewModels racing to update the same ticket), not across multiple physical devices.
- **No cloud durability.** If the device is wiped, the data is gone. There's no backup path yet.

### What would change for production

`API.md` documents the target REST contract this local implementation was designed against — the repository interfaces already mirror what a real backend's endpoints would look like (`/api/v1/queue/tickets/{id}/call`, `/api/v1/billing/payments`, etc.). The intent was that swapping Room for a Room-as-local-cache + REST-as-source-of-truth model (a fairly standard offline-first mobile pattern) would mean changing the repository implementations, not the ViewModels or UI. That's untested in this project, but it's the reason the repository layer exists as a clean seam rather than calling Room directly from ViewModels.

---

## 2. Role-Based Access Control (RBAC) Across 10 Roles

### The decision

CareFlow defines 10 distinct roles (`PATIENT`, `RECEPTIONIST`, `QUEUE_OPERATOR`, `NURSE`, `DOCTOR`, `LAB_TECHNICIAN`, `PHARMACIST`, `CASHIER`, `HOSPITAL_ADMIN`, `SUPER_ADMIN`), enforced at both the navigation layer (what screens a role can reach) and the repository layer (what data operations a role can perform), rather than relying on UI-only restrictions.

### Why

A common shortcut in student/demo projects is to enforce roles only in the UI — hide a button, disable a menu item — while leaving the underlying data layer open to anyone who's authenticated. That's a real vulnerability pattern (client-side-only authorization), and part of the point of this project was to demonstrate the difference between "the UI doesn't show it" and "the system won't allow it."

So CareFlow enforces role checks at the repository level: even if a screen were somehow reached by the wrong role, the underlying data operation checks the current user's role before executing. This mirrors how real healthcare systems have to think about access — a nurse being able to *see* a "prescribe medication" button is a UI bug; a nurse being able to *actually write* a prescription to the database is a compliance and safety failure.

### The trade-off

Ten roles is a lot of surface area to test exhaustively, and the current test suite (27 tests, covering `SecurityAndAuthTest.kt` and `EndToEndHospitalWorkflowTest.kt` among others) covers the core paths rather than every role × every operation permutation. That's an honest limitation — the architecture supports fine-grained enforcement, but the test coverage proving every boundary holds isn't complete yet. Widening that matrix of role/operation tests is the most valuable next step for this project, more so than adding new features.

### Why 10 roles specifically

The role set was modeled on distinct *responsibilities* in a real hospital's patient-flow chain rather than distinct *job titles* — e.g., `QUEUE_OPERATOR` is separated from `RECEPTIONIST` because dispatching patients through departments is a genuinely different responsibility (and failure mode) than registering them, even though in a small clinic the same person might do both. Modeling responsibilities separately, even when they'd sometimes collapse onto one person in reality, keeps the permission boundaries clean and makes the system easier to reason about — and easier to extend later if, say, a hospital wants queue dispatch handled by a separate department.

---

## 3. The Queue State Machine

### The decision

Queue tickets move through an explicit, validated state machine:

```
WAITING → CALLED → IN_CONSULTATION → COMPLETED
```

with two additional terminal states, `SKIPPED` and `CANCELLED`, reachable from earlier states. Every transition is validated before being written — a ticket can't jump from `WAITING` directly to `COMPLETED`, for instance, and duplicate active tickets for the same patient are prevented at the point of creation.

### Why

Queue management is the part of the workflow most exposed to real-world messiness: patients don't show up when called, staff need to skip and recall people, tickets get transferred between departments mid-flow, and multiple staff members (a queue operator and a doctor, say) might try to update the same ticket close together. Treating queue status as a free-form string or enum with no transition rules would let bugs silently corrupt the queue's meaning — e.g., a ticket marked `COMPLETED` that never actually went through `IN_CONSULTATION`, which would quietly break both the "who's currently being seen" dashboard and any reporting built on top of it.

Modeling it explicitly as a state machine, with validation at the write boundary rather than trusted at the UI layer, means the queue's invariants (only one active ticket per patient, no skipped transitions, no invalid recalls) hold regardless of which screen or role triggered the change. This is the same reasoning as the RBAC decision above: don't trust the caller, validate at the boundary that actually matters.

### The trade-off

A stricter state machine is less forgiving of edge cases that a real hospital might need — for example, a ticket needing to move backward (say, a patient recalled from `IN_CONSULTATION` back to `WAITING` because a doctor got pulled into an emergency). The current model treats forward-only transitions as the default and handles exceptions like "skip" and "cancel" as explicit terminal branches rather than allowing arbitrary backward movement. That's a deliberate simplicity trade-off for a demo system — a production version would need a richer set of validated backward transitions, informed by actual hospital operational input rather than assumptions made in isolation.

### Why this matters more than it looks

State machines like this are exactly the kind of thing that's easy to get subtly wrong — allowing a technically-possible-but-nonsensical transition, or forgetting to prevent a duplicate active ticket, or letting a race condition create two `IN_CONSULTATION` tickets for the same patient. Getting the queue state machine right, and being able to explain *why* it's shaped the way it is, is a better demonstration of engineering judgment than most of the surrounding CRUD screens — which is why it's worth a dedicated section here rather than being buried in the feature list.

---

## Summary

Each of these three decisions trades some real capability (multi-device sync, exhaustive role-boundary testing, backward-compatible queue transitions) for a tighter, more defensible core: a clean repository seam ready for a real backend, authorization enforced where it actually matters rather than just in the UI, and a queue model whose invariants can be reasoned about and trusted. None of these are presented as finished production engineering — the README is explicit about that — but each one reflects a real trade-off that was made on purpose, not by default.
