# CareFlow Database ER Model & Schema Specification

The CareFlow local database is designed in Room SQLite with primary keys, indexes, foreign keys, and audit timestamps (`created_at`, `updated_at`).

---

## 1. Relational Entities Diagram (Mermaid)

```mermaid
erDiagram
    USERS ||--o{ AUDIT_LOGS : performs
    USERS ||--o| DOCTORS : links_to
    DEPARTMENTS ||--o{ DOCTORS : contains
    DEPARTMENTS ||--o{ QUEUE_TICKETS : assigns
    PATIENTS ||--o{ EMERGENCY_CONTACTS : has
    PATIENTS ||--o{ APPOINTMENTS : books
    DOCTORS ||--o{ APPOINTMENTS : attends
    PATIENTS ||--o{ QUEUE_TICKETS : queued_as
    PATIENTS ||--o{ VITAL_SIGNS : recorded_for
    PATIENTS ||--o{ CLINICAL_ENCOUNTERS : undergoes
    DOCTORS ||--o{ CLINICAL_ENCOUNTERS : conducts
    CLINICAL_ENCOUNTERS ||--o{ PRESCRIPTIONS : originates
    PATIENTS ||--o{ LAB_ORDERS : requests
    DOCTORS ||--o{ LAB_ORDERS : orders
    PATIENTS ||--o{ INVOICES : billed_to
    USERS ||--o{ NOTIFICATIONS : receives

    USERS {
        string id PK
        string username UK
        string password_hash
        string salt
        string full_name
        string role
        string department_id
        string email
        string phone
        int is_active
        int failed_login_attempts
        long locked_until
        int force_password_change
        long created_at
        long last_login_at
    }

    AUDIT_LOGS {
        string id PK
        long timestamp
        string user_id
        string username
        string user_role
        string action
        string entity_name
        string entity_id
        string details
        string ip_or_device
        string result
    }

    PATIENTS {
        string id PK
        string hospital_reg_no UK
        string full_name
        string dob
        string gender
        string national_id_or_passport
        string phone
        string email
        string address
        string preferred_language
        string blood_group
        string allergies
        string existing_conditions
        string emergency_contact_name
        string emergency_contact_relationship
        string emergency_contact_phone
        string emergency_contact_secondary_phone
        long registered_date
        string status
        long created_at
        long updated_at
        string created_by
        string updated_by
    }

    EMERGENCY_CONTACTS {
        string id PK
        string patient_id FK
        string name
        string relationship
        string phone
        string secondary_phone
        string address
        int is_primary
        long created_at
        long updated_at
    }

    APPOINTMENTS {
        string id PK
        string patient_id FK
        string doctor_id FK
        string department_id FK
        long date_time
        string appointment_type
        string status
        string reason
        string notes
    }

    QUEUE_TICKETS {
        string id PK
        string ticket_number UK
        string patient_id FK
        string department_id FK
        string doctor_id FK
        int priority
        string status
        int estimated_wait_minutes
        long created_at
        long called_at
        long completed_at
    }

    VITAL_SIGNS {
        string id PK
        string patient_id FK
        string recorded_by_user_id FK
        long timestamp
        float temperature
        int systolic_bp
        int diastolic_bp
        int pulse_rate
        int respiratory_rate
        int oxygen_saturation
        float weight_kg
        float height_cm
        int pain_score
        string notes
        int urgent_attention_flag
    }

    CLINICAL_ENCOUNTERS {
        string id PK
        string patient_id FK
        string doctor_id FK
        long timestamp
        string chief_complaint
        string history
        string examination_notes
        string assessment
        string clinician_diagnosis
        string treatment_plan
        string status
    }

    PRESCRIPTIONS {
        string id PK
        string encounter_id FK
        string patient_id FK
        string doctor_id FK
        string status
        string items_json
        string instructions
        long created_at
    }

    LAB_ORDERS {
        string id PK
        string patient_id FK
        string doctor_id FK
        string test_name
        string priority
        string status
        long ordered_at
        long sample_collected_at
        string result_json
        string verified_by_user_id FK
        long verified_at
    }

    INVOICES {
        string id PK
        string invoice_number UK
        string patient_id FK
        double total_amount
        double paid_amount
        string status
        string items_json
        long created_at
    }
```

---

## 2. Status Transitions Engine

### Patient Master Record Lifecycle:
`ACTIVE` ➔ `INACTIVE` ➔ `ARCHIVED`  
*(Special terminal states: `DECEASED`, `TRANSFERRED`)*  
*Note: Hard deletion is prohibited in this demonstration environment. Archival requires `ARCHIVE_PATIENT` administrative authority.*

### Hospital Registration Number Standard:
* Format: `CF-YYYY-XXXXXX` (e.g. `CF-2026-000001`)
* Strategy: Sequential 6-digit zero-padded index per calendar year, decoupled from patient demographic names, indexed uniquely in Room SQLite.

### Queue Lifecycle:
`WAITING` ➔ `CALLED` ➔ `IN_CONSULTATION` ➔ `COMPLETED`  
*(Exceptions: `SKIPPED`, `CANCELLED`)*

### Appointment Lifecycle:
`REQUESTED` ➔ `CONFIRMED` ➔ `CHECKED_IN` ➔ `COMPLETED`  
*(Exceptions: `RESCHEDULED`, `CANCELLED`, `NO_SHOW`)*

### Prescription Lifecycle:
`DRAFT` ➔ `ISSUED` ➔ `DISPENSING` ➔ `DISPENSED`  
*(Exceptions: `PARTIALLY_DISPENSED`, `CANCELLED`)*

### Laboratory Order Lifecycle:
`REQUESTED` ➔ `SAMPLE_COLLECTED` ➔ `PROCESSING` ➔ `COMPLETED` ➔ `VERIFIED`  
*(Exception: `CANCELLED`)*

### Invoice Billing Lifecycle:
`PENDING` ➔ `PARTIALLY_PAID` ➔ `PAID`  
*(Exceptions: `CANCELLED`, `REFUNDED`)*
