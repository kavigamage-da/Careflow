# CareFlow Hospital Management System

CareFlow is an Android hospital workflow management system developed as an academic and portfolio software project. It demonstrates secure role-based digital workflows across patient reception, queue management, clinical documentation, laboratory, pharmacy, billing, and operational reporting.

> **CRITICAL MEDICAL & CLINICAL SAFETY NOTICE**
> CareFlow is an operational, information-management, and decision-support tool. It does **NOT** automatically diagnose patients, prescribe medications, or replace the clinical judgment of licensed healthcare professionals. All medical decisions remain the sole responsibility of qualified practitioners.

---

## 🏥 Operational Flow

```
[Patient Arrival] ──► [Reception / Registration] ──► [Appointment & Check-In]
                                                               │
                                                               ▼
[Cashier Billing & Receipt] ◄── [Pharmacy Dispense] ◄── [Queue Dispatch]
            ▲                             ▲                    │
            │                             │                    ▼
    [Reports & Admin]             [Lab Diagnostic]     [Nurse Triage & Vitals]
            ▲                             ▲                    │
            │                             │                    ▼
    [Audit Governance] ◄────────── [Doctor Consultation & Prescription]
```

---

## 🚀 Key Capabilities (Implemented Phases 1 - 9)

- **Phase 1 — Security, Auth & RBAC:**
  - 10 hospital roles (`PATIENT`, `RECEPTIONIST`, `QUEUE_OPERATOR`, `DOCTOR`, `NURSE`, `LAB_TECHNICIAN`, `PHARMACIST`, `CASHIER`, `HOSPITAL_ADMIN`, `SUPER_ADMIN`).
  - Salted SHA-256 password security with per-password salt, 5-attempt rate-limiting, account lockout, inactivity session timeout, and append-oriented security audit logging (`AuditLogEntity`).
- **Phase 2 — Master Patient Index (MPI):**
  - Demographic registration with automated MRN generation (`CF-YYYY-XXXXXX`), format validation, and duplicate detection by name + DOB, national ID, or phone.
  - Patient search, comprehensive medical profile, and demographic updates.
- **Phase 3 — Queue Management & Check-In:**
  - Multi-department ticket issuance (Cardiology, General Medicine, Pediatrics, Orthopedics, Emergency).
  - Priority triage levels: Standard, Fast Track, Urgent, Emergency.
  - Real-time queue state transitions: `WAITING` ➔ `CALLED` ➔ `IN_CONSULTATION` ➔ `COMPLETED` / `SKIPPED`.
  - Paging, recall tracking, department transfer, and duplicate ticket prevention.
- **Phase 4 — Nurse Triage & Vital Signs:**
  - Standard physiological assessment: Blood Pressure (systolic/diastolic), Pulse, Temperature, SpO2, Respiratory Rate, Height, Weight, and automatic BMI calculation.
  - Pain score rating (0–10), urgent clinical attention flag, and priority escalation.
- **Phase 5 — Doctor Consultation Workbench:**
  - Clinical documentation: Chief complaint, history of present illness, physical examination, assessment, clinician diagnosis, treatment plan, and follow-up instructions.
  - Electronic prescription ordering with drug dosage, frequency, and instructions.
  - Diagnostic laboratory test ordering with priority flags (Routine, Urgent, STAT).
  - Automatic invoice generation upon consultation conclusion.
- **Phase 6 — Pharmacy Dispensing:**
  - Workstation displaying active prescriptions, itemized medication verification, batch safety review, and dispensing state tracking.
- **Phase 7 — Laboratory Diagnostics:**
  - Workstation for specimen collection, test processing, qualitative/quantitative findings entry, reference interval notes, and supervisory verification.
- **Phase 8 — Cashier & Billing:**
  - Itemized patient billing combining consultation, pharmacy, and laboratory fees.
  - Payment processing (Cash, Card, Insurance) with partial/full settlement and in-app payment receipt generation for demonstration purposes.
- **Phase 9 — Operational Reporting & Governance:**
  - Real-time aggregation of hospital KPIs: patient volume, consultation throughput, queue latency, diagnostic orders, pharmacy volume, and revenue accounts.
  - Security audit log inspection.

---

## 🎬 Recommended End-to-End Demo Flow

1. **LOGIN AS RECEPTIONIST** (`reception.ann` / `Password123!`):
   - Navigate to **Search Patients** or **Register Patient**.
   - Open patient profile (e.g. John Doe, `CF-2026-000001`).
   - Click **Check-In to Queue** ➔ Select Department (e.g., General Medicine) and Priority ➔ Issue Ticket (e.g. `G-101`).
2. **LOGIN AS QUEUE OPERATOR** (`queue.operator` / `Password123!`):
   - Open **Queue Dispatch Console**.
   - Locate ticket `G-101` ➔ Click **Call** (pages patient to consultation room).
3. **LOGIN AS NURSE** (`nurse.clara` / `Password123!`):
   - From Dashboard or Queue, select ticket `G-101` ➔ Click **Triage Vitals**.
   - Review or adjust Blood Pressure, Pulse, SpO2, Temperature, Height/Weight (auto-calculates BMI).
   - Click **Save Vitals & Complete Triage**.
4. **LOGIN AS DOCTOR** (`doctor.smith` / `Password123!`):
   - Open **Consultation Queue** ➔ Click **Start Consult** on ticket `G-101`.
   - Review vitals summary ➔ Document Chief Complaint, Examination, and Diagnosis.
   - Prescribe medications (e.g., Amoxicillin 500mg) ➔ Select Lab tests (e.g., Complete Blood Count).
   - Click **Finalize Consultation & Dispatch Orders**.
5. **LOGIN AS LAB TECHNICIAN** (`lab.tech` / `Password123!`):
   - Open **Laboratory Diagnostics**.
   - Click **Collect Specimen** ➔ Click **Enter Result** (enter findings) ➔ Click **Verify & Release**.
6. **LOGIN AS PHARMACIST** (`pharma.alex` / `Password123!`):
   - Open **Pharmacy Dispensing Console**.
   - Review issued prescription ➔ Click **Verify & Dispense**.
7. **LOGIN AS CASHIER** (`cashier.david` / `Password123!`):
   - Open **Cashier Desk** ➔ Select pending invoice for patient.
   - Click **Pay Bill** ➔ Select Cash/Card ➔ Confirm payment ➔ Click **View Receipt**.
8. **LOGIN AS HOSPITAL ADMIN / SUPER ADMIN** (`admin` / `Password123!`):
   - Open **Operational Reports** to inspect updated patient throughput, consultation counts, and revenue.
   - Open **Security Audit Logs** to review append-oriented security audit log of all events.

---

## 🛠 Tech Stack

- **Platform:** Android (Min SDK 24, Target SDK 36)
- **Language:** Kotlin 2.2+ (Coroutines, Flow, StateFlow)
- **UI Toolkit:** Jetpack Compose + Material 3 Design System
- **Navigation:** Navigation Compose with role-guarded routing
- **Local Persistence:** Room Database (KSP) + Jetpack DataStore
- **Architecture:** Clean Architecture + Repository Pattern + Dependency Injection
- **Network / Future API:** Retrofit / OkHttp / Moshi

---

## Verification Status

CareFlow has been verified through:

- **Unit tests** for core business logic (authentication, RBAC, patient validation, queue state machine, BMI calculation, billing calculations)
- **End-to-end workflow tests** verifying data chain integrity across patient registration, queue, triage, consultation, prescriptions, laboratory, pharmacy, and billing
- **Security verification** including password hashing, RBAC enforcement, and audit logging
- **Database verification** including entity relationships, foreign keys, indexes, and migrations
- **Demo data verification** confirming all demo accounts use demonstration data only

### Build

```bash
./gradlew :app:assembleDebug
```

### Tests

```bash
./gradlew :app:testDebugUnitTest
```

> CareFlow is an academic and portfolio software project using local Room storage and demonstration data. It is not presented as a production hospital information system.
