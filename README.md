# CareFlow — Hospital Workflow Management System

<p align="center">
  <strong>Android-based Hospital Workflow & Information Management System</strong><br>
  Academic & Portfolio Software Project
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Database-Room-6DB33F?style=for-the-badge" alt="Room">
</p>

<p align="center">
  <a href="https://github.com/kavigamage-da/CareFlow">
    <img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github" alt="GitHub Repository">
  </a>
</p>

---

## 📌 Overview

**CareFlow** is an Android hospital workflow and information-management system developed as an academic and portfolio software project.

The system demonstrates how multiple hospital operational roles can work through a connected digital workflow covering:

* Patient registration and management
* Queue and check-in management
* Nurse triage and vital signs
* Doctor consultation documentation
* Prescription management
* Laboratory workflows
* Pharmacy dispensing
* Billing and payments
* Operational reporting
* Security and audit logging

CareFlow is designed to demonstrate **role-based workflows, local data management, business rules, security controls, and end-to-end information flow** within a healthcare-oriented software environment.

> **⚠️ Medical & Clinical Safety Notice**
>
> CareFlow is an operational, information-management, and decision-support software project. It does **not** automatically diagnose patients, independently prescribe medication, or replace the professional judgment of qualified healthcare practitioners.
>
> The clinical information represented in the application is intended for demonstration and academic purposes.

---

# 🏥 Hospital Workflow

```text
                         ┌──────────────────────┐
                         │   Patient Arrival    │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ Reception / Check-In │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   Queue Management   │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ Nurse Triage / Vitals│
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ Doctor Consultation  │
                         └───────┬───────┬──────┘
                                 │       │
                    ┌────────────┘       └────────────┐
                    ▼                                 ▼
          ┌──────────────────┐              ┌──────────────────┐
          │ Laboratory       │              │ Prescription     │
          │ Diagnostics      │              │                  │
          └────────┬─────────┘              └────────┬─────────┘
                   │                                 │
                   ▼                                 ▼
          ┌──────────────────┐              ┌──────────────────┐
          │ Results / Verify │              │ Pharmacy         │
          └────────┬─────────┘              │ Dispensing       │
                   │                        └────────┬─────────┘
                   └──────────────┬─────────────────┘
                                  ▼
                         ┌──────────────────────┐
                         │ Billing & Payment    │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ Reports & Governance │
                         └──────────────────────┘
```

---

# 👥 Role-Based Access

CareFlow implements role-based access control across **10 hospital roles**:

| Role             | Main Responsibility                      |
| ---------------- | ---------------------------------------- |
| `PATIENT`        | Patient-facing access                    |
| `RECEPTIONIST`   | Patient registration and check-in        |
| `QUEUE_OPERATOR` | Queue dispatch and ticket management     |
| `NURSE`          | Triage and vital-sign recording          |
| `DOCTOR`         | Consultation and clinical documentation  |
| `LAB_TECHNICIAN` | Laboratory specimen and result workflow  |
| `PHARMACIST`     | Prescription verification and dispensing |
| `CASHIER`        | Billing and payment processing           |
| `HOSPITAL_ADMIN` | Operational reporting and administration |
| `SUPER_ADMIN`    | System-level administration              |

Access to protected operations is enforced through role-aware application and repository-level checks.

---

# 🚀 Implemented Modules

## 🔐 1. Authentication, Security & RBAC

* 10-role access-control model
* Salted SHA-256 password hashing
* Cryptographically generated per-password salt
* Password policy enforcement
* Failed-login attempt tracking
* Account lockout after repeated failures
* Configurable inactivity session timeout
* Session token handling
* Role-based navigation and authorization
* Append-oriented security audit logging

---

## 👤 2. Patient Management

* Patient registration
* Automated Medical Record Number generation

```text
CF-YYYY-XXXXXX
```

* Patient demographic information
* Patient search
* Patient profile
* Demographic updates
* Duplicate detection using available patient attributes
* Patient-related workflow history

---

## 🎫 3. Queue Management & Check-In

Supported workflow includes:

```text
WAITING
   ↓
CALLED
   ↓
IN_CONSULTATION
   ↓
COMPLETED
```

Additional states include:

```text
SKIPPED
CANCELLED
```

Features include:

* Department-based queue tickets
* Standard priority
* Fast-track priority
* Urgent priority
* Emergency priority
* Ticket paging
* Recall tracking
* Department transfer
* Duplicate active-ticket prevention
* Queue-state validation
* Concurrency-aware queue updates

Example ticket:

```text
G-101
```

---

## 🩺 4. Nurse Triage & Vital Signs

The triage workflow supports recording:

* Blood pressure
* Pulse
* Temperature
* SpO₂
* Respiratory rate
* Height
* Weight
* BMI calculation
* Pain score
* Clinical attention flag
* Priority escalation

Example:

```text
Height: 172 cm
Weight: 70 kg

BMI ≈ 23.7
```

The system validates entered measurements against configured bounds before storing them.

---

## 👨‍⚕️ 5. Doctor Consultation

The consultation workflow supports documentation of:

* Chief complaint
* History of present illness
* Physical examination
* Assessment
* Clinician-entered diagnosis
* Treatment plan
* Follow-up instructions

The doctor can also create:

### Prescriptions

* Medication
* Dosage
* Frequency
* Instructions
* Prescription workflow state

### Laboratory Orders

* Laboratory test
* Priority
* Request information

Consultation completion can also generate the associated billing workflow.

---

## 💊 6. Pharmacy Dispensing

The pharmacy workflow supports:

* Viewing issued prescriptions
* Prescription review
* Medication verification
* Dispensing workflow
* Dispensing state tracking

Example workflow:

```text
ISSUED
   ↓
DISPENSING
   ↓
DISPENSED
```

> The current implementation does not claim to provide full pharmacy inventory management.

---

## 🧪 7. Laboratory Diagnostics

The laboratory workflow supports:

```text
REQUESTED
    ↓
SAMPLE_COLLECTED
    ↓
PROCESSING
    ↓
COMPLETED
    ↓
VERIFIED
```

Features include:

* Laboratory requests
* Specimen collection
* Processing workflow
* Qualitative / quantitative result entry
* Reference information
* Result verification
* Result release

---

## 💳 8. Billing & Payments

The billing module supports:

* Consultation charges
* Laboratory charges
* Pharmacy-related charges
* Itemized invoices
* Cash payments
* Card payments
* Insurance payments
* Partial settlement
* Full settlement
* Payment calculation
* Demonstration payment receipts

Payment calculations use decimal-safe monetary handling.

---

## 📊 9. Operational Reporting

The reporting module aggregates information from the local database for operational dashboards.

Examples include:

* Patient volume
* Consultation activity
* Queue information
* Laboratory activity
* Pharmacy activity
* Revenue information
* Operational counts

The reporting layer uses application data rather than hardcoded demonstration KPI values.

---

## 🛡️ 10. Audit & Governance

Critical application activities can be recorded in an append-oriented security audit log.

The audit workflow is intended to support:

* Security monitoring
* User-action traceability
* Administrative review
* Demonstration of governance controls

> The current audit implementation should not be interpreted as a cryptographically tamper-proof or immutable ledger.

---

# 🗄️ Data & Persistence

CareFlow uses an offline-first local persistence approach.

### Room Database

The project contains Room entities covering areas such as:

* Users
* Patients
* Queue tickets
* Clinical encounters
* Vital signs
* Prescriptions
* Laboratory workflows
* Pharmacy workflows
* Billing
* Payments
* Audit events

Database implementation includes:

* Entity relationships
* Foreign keys
* Indexes
* Type converters
* Database migrations
* Repository-based data access

---

# 🏗️ Architecture

CareFlow follows a layered application structure based around:

```text
┌─────────────────────────────┐
│       Jetpack Compose UI    │
├─────────────────────────────┤
│        ViewModels           │
├─────────────────────────────┤
│   Repository / Business     │
│          Logic              │
├─────────────────────────────┤
│        Room Database        │
├─────────────────────────────┤
│       Local Storage         │
└─────────────────────────────┘
```

Key architectural concepts include:

* Separation of UI and data responsibilities
* Repository pattern
* ViewModel-based state management
* Kotlin Coroutines
* Flow / StateFlow
* Room persistence
* Role-aware navigation
* Centralized application dependencies

---

# 🛠️ Technology Stack

| Area                    | Technology                            |
| ----------------------- | ------------------------------------- |
| Platform                | Android                               |
| Language                | Kotlin                                |
| UI                      | Jetpack Compose                       |
| Design System           | Material 3                            |
| Navigation              | Navigation Compose                    |
| Database                | Room                                  |
| Database Processing     | KSP                                   |
| Local Preferences       | Jetpack DataStore                     |
| Asynchronous Processing | Kotlin Coroutines                     |
| Reactive State          | Flow / StateFlow                      |
| Architecture            | Repository-based layered architecture |
| Build System            | Gradle                                |
| IDE                     | Android Studio                        |

### Android Configuration

```text
Minimum SDK: 24
Target SDK: 36
```

---

# 🔄 End-to-End Demonstration Workflow

A complete demonstration can follow this workflow:

```text
Reception
   ↓
Patient Search / Registration
   ↓
Check-In
   ↓
Queue Ticket
   ↓
Queue Dispatch
   ↓
Nurse Triage
   ↓
Doctor Consultation
   ├──────────────► Laboratory
   │                   ↓
   │               Result Verification
   │
   └──────────────► Prescription
                       ↓
                   Pharmacy
                       ↓
                    Billing
                       ↓
                    Receipt
                       ↓
               Reports & Audit Log
```

### Suggested Demo Accounts

All demonstration accounts use:

```text
Password123!
```

Example accounts:

```text
reception.ann
queue.operator
nurse.clara
doctor.smith
lab.tech
pharma.alex
cashier.david
admin
```

> These credentials are for the local demonstration environment only. They must not be treated as production credentials.

---

# 🧪 Verification & Testing

The repository contains automated test definitions covering areas including:

* Authentication and security
* Role-based access
* Patient management
* Queue workflow
* End-to-end hospital workflow

Current test definitions include:

```text
SecurityAndAuthTest.kt
PatientManagementTest.kt
EndToEndHospitalWorkflowTest.kt
ExampleUnitTest.kt
ExampleRobolectricTest.kt
ExampleInstrumentedTest.kt
```

The project contains **27 defined tests** in the current test suite.

Additional areas identified for future test expansion include:

* Room persistence and relationships
* Database migrations
* Audit-log verification
* Repository integration testing

### Build

```bash
./gradlew :app:assembleDebug
```

### Unit Tests

```bash
./gradlew :app:testDebugUnitTest
```

> **Verification note:** The commands above are the intended project verification commands. Local execution may require a correctly configured Android SDK and compatible Android command-line tools.

---

# 📱 Screens / Workflow Areas

The application includes workflow areas for:

```text
Authentication
Dashboard
Patient Management
Queue Management
Nurse Triage
Doctor Consultation
Prescriptions
Laboratory
Pharmacy
Billing
Reports
Audit Logs
```

---

# 🔒 Security Considerations

CareFlow demonstrates several application-level security controls:

* Salted password hashing
* Per-password cryptographic salt
* Password policy enforcement
* Login failure handling
* Account lockout
* Session timeout
* Role-based authorization
* Repository-level permission checks
* Parameterized Room queries
* Audit logging
* No hardcoded API secrets
* No hardcoded signing credentials

CareFlow is nevertheless an **academic and portfolio project**, not a production-certified hospital information system.

---

# 🚧 Current Scope & Limitations

The current implementation intentionally focuses on a local demonstration environment.

The following are **not currently presented as implemented production capabilities**:

* Cloud synchronization
* Multi-device real-time synchronization
* Real hospital-system integration
* FHIR/HL7 integration
* Production payment gateway
* Biometric authentication
* Push notifications / FCM
* Full pharmacy inventory management
* Real-world insurance integration
* Production deployment infrastructure
* Dedicated emergency-contact management UI
* Cryptographically tamper-evident audit ledger

These areas could be considered future extensions rather than current capabilities.

---

# 🗺️ Future Development

Potential future extensions include:

* Cloud-backed multi-device synchronization
* Hospital interoperability
* FHIR-based healthcare data exchange
* Push notifications
* Appointment scheduling
* Pharmacy inventory management
* Advanced analytics
* Backup and recovery infrastructure
* Stronger audit integrity mechanisms
* Production-grade authentication infrastructure
* Automated CI/CD testing

---

# 🎓 Academic & Portfolio Context

CareFlow was developed to demonstrate practical application of:

* Software engineering
* Database design
* Mobile application development
* Role-based access control
* Business workflow modelling
* Information management
* Security concepts
* Requirements analysis
* End-to-end workflow design
* Testing and verification

The project focuses on demonstrating how a complex operational workflow can be translated into a structured Android application.

---

# ⚠️ Project Disclaimer

CareFlow is an **academic and portfolio software project**.

It uses demonstration data and local Room storage and is not presented as a production hospital information system.

It must not be used to make real clinical decisions, diagnose medical conditions, prescribe medication, or replace qualified healthcare professionals.

---

# 👩‍💻 Developer

**Kavindi Gamage**

### Connect

* GitHub: https://github.com/kavigamage-da
* LinkedIn: https://www.linkedin.com/in/kavindi-gamage-815049386
 

---

## ⭐ Project

If you find the project useful for learning about Android development, healthcare workflows, role-based systems, or software engineering, consider giving the repository a ⭐.

**Repository:**
https://github.com/kavigamage-da/CareFlow
