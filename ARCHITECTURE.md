# CareFlow Architecture Specification

## 1. Clean Architecture Layers

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   Jetpack Compose UI • ViewModels • M3 Components      │
└───────────────────────────┬────────────────────────────┘
                            │ observes StateFlow
                            ▼
┌────────────────────────────────────────────────────────┐
│                      Domain Layer                      │
│   Use Cases • RolePermissionMatrix • Domain Models     │
│             Repository Interfaces (Contracts)          │
└───────────────────────────┬────────────────────────────┘
                            │ depends on
                            ▼
┌────────────────────────────────────────────────────────┐
│                       Data Layer                       │
│  Repository Implementations (Offline-First / Hybrid)   │
│   ┌───────────────────────┐   ┌─────────────────────┐  │
│   │ Local Source (Room)   │   │ Remote API (REST)   │  │
│   │ DAOs & SQLite Engine  │   │ Retrofit / OkHttp   │  │
│   └───────────────────────┘   └─────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

---

## 2. Package Structure

```
com.example
├── CareFlowApplication.kt            # Application subclass & container lifecycle
├── MainActivity.kt                   # Single Activity entry point
├── di
│   ├── AppContainer.kt               # Dependency Injection / Service Locator
│   └── LocalAppContainer.kt          # CompositionLocal provider for UI
├── core
│   ├── model
│   │   ├── UserRole.kt               # 10 hospital roles
│   │   ├── Permission.kt             # Granular functional permissions
│   │   ├── RolePermissionMatrix.kt   # Authorization evaluator
│   │   ├── AuthUser.kt               # Session & user domain model
│   │   ├── AuditLog.kt               # Security audit record
│   │   └── HospitalSetting.kt        # System configuration entity
│   ├── security
│   │   ├── SecurityManager.kt        # Password hashing (SHA-256 with per-password salt) & tokens
│   │   └── SessionManager.kt         # Inactivity tracking & session lifecycle
│   └── util
│       ├── Result.kt                 # Sealed class for Success/Error/Loading
│       └── DateTimeUtils.kt          # Date formatting & timestamps
├── data
│   ├── local
│   │   ├── CareFlowDatabase.kt       # Room Database definition & pre-population
│   │   ├── Converters.kt             # TypeConverters for dates and enums
│   │   ├── dao
│   │   │   ├── UserDao.kt
│   │   │   ├── AuditLogDao.kt
│   │   │   ├── HospitalSettingDao.kt
│   │   │   ├── DepartmentDao.kt
│   │   │   ├── DoctorDao.kt
│   │   │   ├── PatientDao.kt
│   │   │   ├── AppointmentDao.kt
│   │   │   ├── QueueDao.kt
│   │   │   ├── ClinicalDao.kt
│   │   │   ├── PrescriptionDao.kt
│   │   │   ├── LabDao.kt
│   │   │   ├── BillingDao.kt
│   │   │   └── NotificationDao.kt
│   │   └── entity                    # Room relational entities
│   │       ├── UserEntity.kt
│   │       ├── AuditLogEntity.kt
│   │       ├── HospitalSettingEntity.kt
│   │       ├── DepartmentEntity.kt
│   │       ├── DoctorEntity.kt
│   │       ├── PatientEntity.kt
│   │       ├── AppointmentEntity.kt
│   │       ├── QueueTicketEntity.kt
│   │       ├── VitalSignEntity.kt
│   │       ├── ClinicalEncounterEntity.kt
│   │       ├── PrescriptionEntity.kt
│   │       ├── LabOrderEntity.kt
│   │       ├── InvoiceEntity.kt
│   │       └── NotificationEntity.kt
│   └── repository
│       ├── AuthRepository.kt & AuthRepositoryImpl.kt
│       ├── AuditRepository.kt & AuditRepositoryImpl.kt
│       ├── SettingsRepository.kt & SettingsRepositoryImpl.kt
│       └── interfaces for future phases...
├── ui
│   ├── components
│   │   ├── CareFlowScaffold.kt       # Role-aware top bar, demo banner, drawer
│   │   ├── CareFlowCard.kt           # KPI & metric cards
│   │   ├── StatusChip.kt             # Status tags with semantic colors
│   │   └── CommonButtons.kt
│   ├── theme
│   │   ├── Color.kt                  # Healthcare Teal palette
│   │   ├── Theme.kt                  # Material 3 dynamic color scheme
│   │   └── Type.kt
│   ├── navigation
│   │   ├── Screen.kt                 # Type-safe navigation routes
│   │   └── AppNavHost.kt             # Main navigation graph & guards
│   ├── auth
│   │   ├── LoginScreen.kt
│   │   ├── LoginViewModel.kt
│   │   ├── ForgotPasswordScreen.kt
│   │   └── ChangePasswordScreen.kt
│   ├── dashboard
│   │   ├── RoleDashboardScreen.kt    # Role-specific dashboard dispatcher
│   │   └── RoleDashboardViewModel.kt
│   ├── audit
│   │   ├── AuditLogScreen.kt
│   │   └── AuditLogViewModel.kt
│   └── settings
│       ├── HospitalSettingsScreen.kt
│       └── HospitalSettingsViewModel.kt
```

---

## 3. Local vs. Backend Integration Matrix

| Feature | Local (Offline-First Room) | Backend Service Required |
|---|---|---|
| User Authentication & Role Validation | ✅ Local hash verify + session token | Remote OAuth2/SSO & token refresh |
| Audit Trail | ✅ SQLite local append-oriented logging | Remote SIEM / PostgreSQL audit replica |
| Patient Demographics | ✅ Cached in Room | Master Patient Index (MPI) / Central DB |
| Queue Tickets & Dispatch | ✅ Local queue state machine | Real-time WebSocket / MQTT Push to Displays |
| Nursing Vitals & Encounters | ✅ Local offline entry | Encrypted EHR / HL7 FHIR sync |
| Prescriptions & Dispensing | ✅ Local inventory transaction | Central Pharmacy ERP & e-prescribing |
| Lab Requests & Results | ✅ Local status machine | LIS (Laboratory Information System) drivers |
| Billing & Payments | ✅ Invoice creation & receipt calculation | Payment Gateway (Stripe/Card reader) |
| System Alerts | ✅ In-app notifications | FCM (Firebase Cloud Messaging) Push |

---

## 4. Implementation Roadmap

- **Phase 1 (Completed):** Foundation, Architecture, Room Database, Auth, RBAC, Audit Logging, Dashboards for all 10 roles, Settings.
- **Phase 2:** Patient Management (Registration, Search, Duplicate Detection, Medical History).
- **Phase 3:** Appointments & Real-Time Queue Management (Priority tickets, Doctor dispatch).
- **Phase 4:** Triage & Nursing (Vitals monitoring, Urgent flags, Clinical encounter timeline).
- **Phase 5:** Prescription & Pharmacy Inventory (Batch numbers, expiry alerts, dispensing).
- **Phase 6:** Laboratory Diagnostics (Sample collection, technician result entry, release).
- **Phase 7:** Billing & Payment Tracking (Invoices, receipts, breakdown).
- **Phase 8:** Hospital Notification Center & Reminders.
- **Phase 9:** Executive Reporting, Workload Analytics, & Audit Analysis.
- **Phase 10:** Background Sync via WorkManager & Security Hardening.
