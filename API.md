# CareFlow REST API Specification

> ?? Not yet implemented — target REST contract for a future backend phase.

This contract defines the target backend REST interfaces for the hospital server. In Phase 1, repository interfaces reflect these endpoints, with SQLite/Room acting as the local authoritative cache.

```http
### Authentication
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh
POST   /api/v1/auth/change-password
POST   /api/v1/auth/forgot-password

### Patients
GET    /api/v1/patients?q={query}&status={status}&page={page}
POST   /api/v1/patients
GET    /api/v1/patients/{id}
PUT    /api/v1/patients/{id}

### Appointments
GET    /api/v1/appointments?doctorId={id}&date={isoDate}
POST   /api/v1/appointments
PUT    /api/v1/appointments/{id}/status

### Queue Management
GET    /api/v1/queue/live?departmentId={id}
POST   /api/v1/queue/tickets
POST   /api/v1/queue/tickets/{id}/call
POST   /api/v1/queue/tickets/{id}/complete

### Triage & Clinical
POST   /api/v1/patients/{id}/vitals
GET    /api/v1/patients/{id}/vitals/history
POST   /api/v1/encounters
GET    /api/v1/patients/{id}/encounters

### Prescriptions & Pharmacy
POST   /api/v1/prescriptions
GET    /api/v1/pharmacy/pending
POST   /api/v1/pharmacy/dispense

### Laboratory
POST   /api/v1/lab/orders
GET    /api/v1/lab/worklist
POST   /api/v1/lab/orders/{id}/results
POST   /api/v1/lab/orders/{id}/verify

### Billing & Payments
POST   /api/v1/billing/invoices
GET    /api/v1/billing/invoices/{id}
POST   /api/v1/billing/payments

### Governance & Audit
GET    /api/v1/audit/logs?from={ts}&to={ts}&action={act}
GET    /api/v1/reports/daily-kpi
```

