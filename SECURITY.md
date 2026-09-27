# CareFlow Security & Compliance Specification

## 1. Threat Model & Mitigations

| Threat | Risk Level | Mitigation Strategy |
|---|---|---|
| **Brute Force Credential Attack** | High | Maximum 5 consecutive failed login attempts locks the user account for 15 minutes. All failures emit an append-oriented `LOGIN_FAILED` audit event. |
| **Plaintext Credential Exposure** | Critical | Passwords are never stored in plaintext. They are salted with 16-byte cryptographically secure random salts and hashed via SHA-256. |
| **Privilege Escalation** | Critical | Fine-grained `RolePermissionMatrix` checked at domain and repository layers before every sensitive database operation. UI visibility hiding is never relied upon alone. |
| **Session Hijacking / Idle Terminal** | High | Inactivity timeout policy (default 30 minutes, configurable by Hospital Admin). Session token invalidation upon explicit logout. |
| **Repudiation / Unmonitored Actions** | High | Automated write-ahead `AuditLogEntity` recording timestamp, actor ID, actor role, target entity, outcome, and client session context. |
| **SQL Injection** | Critical | Full adoption of Android Room with parameterized SQLite queries. Dynamic query string concatenation is strictly banned. |
| **Sensitive Log Leakage** | Medium | Passwords, medical diagnosis notes, and personal identifiers are scrubbed from standard Logcat output. |

---

## 2. Role-Permission Matrix

| Permission | PATIENT | RECEPTIONIST | QUEUE_OPERATOR | DOCTOR | NURSE | LAB_TECH | PHARMACIST | CASHIER | HOSPITAL_ADMIN | SUPER_ADMIN |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| `VIEW_OWN_PROFILE` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `REGISTER_PATIENT` | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| `VIEW_PATIENTS` | ❌ | ✅ | ❌ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ |
| `BOOK_APPOINTMENT` | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| `MANAGE_QUEUE` | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| `CALL_NEXT_PATIENT`| ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| `RECORD_VITALS` | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ |
| `CLINICAL_CONSULT`| ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `ORDER_PRESCRIPTION`| ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `DISPENSE_MEDS` | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `ORDER_LAB_TEST` | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `ENTER_LAB_RESULT`| ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `VERIFY_LAB_RESULT`| ❌ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `CREATE_INVOICE` | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| `RECORD_PAYMENT` | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| `VIEW_AUDIT_LOGS`| ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| `MANAGE_SETTINGS`| ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| `MANAGE_STAFF` | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

---

## 3. Demo vs. Production Segregation

- In **Demo Mode**, clearly highlighted with a header banner: `DEMO ENVIRONMENT - CareFlow Demo Hospital`.
- Demo users are initialized with standard mock credentials for testing and evaluation.
- This is a demonstration environment. For production deployment, additional security measures would be required including:
  1. Disabling the demo database seed callback.
  2. Configuring master hospital encryption keys via secure Android Keystore / HSM.
  3. Integrating real OAuth2 / SAML / Hospital SSO with `AuthRepository`.
  4. Enforcing SSL Pinning and Certificate Transparency for remote endpoints.
