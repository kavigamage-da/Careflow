package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.model.AuditAction
import com.example.core.model.HospitalSettingKeys
import com.example.core.model.UserRole
import com.example.core.security.SecurityManager
import com.example.data.local.dao.AppointmentDao
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.BillingDao
import com.example.data.local.dao.ClinicalDao
import com.example.data.local.dao.DepartmentDao
import com.example.data.local.dao.DoctorDao
import com.example.data.local.dao.EmergencyContactDao
import com.example.data.local.dao.HospitalSettingDao
import com.example.data.local.dao.LabDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.PatientDao
import com.example.data.local.dao.PrescriptionDao
import com.example.data.local.dao.QueueDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AppointmentEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.ClinicalEncounterEntity
import com.example.data.local.entity.DepartmentEntity
import com.example.data.local.entity.DoctorEntity
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.HospitalSettingEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.LabOrderEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PatientEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.QueueTicketEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.VitalSignEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        UserEntity::class,
        AuditLogEntity::class,
        HospitalSettingEntity::class,
        DepartmentEntity::class,
        DoctorEntity::class,
        PatientEntity::class,
        EmergencyContactEntity::class,
        AppointmentEntity::class,
        QueueTicketEntity::class,
        VitalSignEntity::class,
        ClinicalEncounterEntity::class,
        PrescriptionEntity::class,
        LabOrderEntity::class,
        InvoiceEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CareFlowDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun hospitalSettingDao(): HospitalSettingDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun doctorDao(): DoctorDao
    abstract fun patientDao(): PatientDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun queueDao(): QueueDao
    abstract fun clinicalDao(): ClinicalDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun labDao(): LabDao
    abstract fun billingDao(): BillingDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: CareFlowDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add extended demographic columns to patients
                db.execSQL("ALTER TABLE patients ADD COLUMN national_id_or_passport TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE patients ADD COLUMN address TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE patients ADD COLUMN preferred_language TEXT NOT NULL DEFAULT 'English'")
                db.execSQL("ALTER TABLE patients ADD COLUMN emergency_contact_relationship TEXT NOT NULL DEFAULT 'Family'")
                db.execSQL("ALTER TABLE patients ADD COLUMN emergency_contact_secondary_phone TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE patients ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE patients ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE patients ADD COLUMN created_by TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE patients ADD COLUMN updated_by TEXT NOT NULL DEFAULT ''")

                // Indices on patients
                db.execSQL("CREATE INDEX IF NOT EXISTS index_patients_email ON patients(email)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_patients_national_id_or_passport ON patients(national_id_or_passport)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_patients_status ON patients(status)")

                // Create emergency_contacts table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS emergency_contacts (
                        id TEXT PRIMARY KEY NOT NULL,
                        patient_id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        relationship TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        secondary_phone TEXT NOT NULL DEFAULT '',
                        address TEXT NOT NULL DEFAULT '',
                        is_primary INTEGER NOT NULL DEFAULT 1,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        FOREIGN KEY(patient_id) REFERENCES patients(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_emergency_contacts_patient_id ON emergency_contacts(patient_id)")
            }
        }

        fun getInstance(context: Context, scope: CoroutineScope): CareFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CareFlowDatabase::class.java,
                    "careflow_hospital.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseSeedCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeedCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    seedDatabase(database)
                }
            }
        }

        private suspend fun seedDatabase(db: CareFlowDatabase) {
            val now = System.currentTimeMillis()

            // 1. Hospital Settings
            val settings = listOf(
                HospitalSettingEntity(HospitalSettingKeys.HOSPITAL_NAME, "CareFlow Demo Hospital", "Official Facility Name", now),
                HospitalSettingEntity(HospitalSettingKeys.EMERGENCY_CONTACT, "+1 (555) 911-CARE", "24/7 Triage & Trauma Dispatch", now),
                HospitalSettingEntity(HospitalSettingKeys.HOSPITAL_ADDRESS, "742 Healthcare Boulevard, Metro Medical District", "Physical Facility Address", now),
                HospitalSettingEntity(HospitalSettingKeys.HOSPITAL_EMAIL, "operations@careflow-demo.health", "Primary Contact Email", now),
                HospitalSettingEntity(HospitalSettingKeys.APPOINTMENT_SLOT_MINUTES, "20", "Standard consultation duration (minutes)", now),
                HospitalSettingEntity(HospitalSettingKeys.SESSION_TIMEOUT_MINUTES, "30", "Staff terminal inactivity timeout in minutes", now),
                HospitalSettingEntity(HospitalSettingKeys.MAX_LOGIN_ATTEMPTS, "5", "Failed attempts before temporary lockout", now),
                HospitalSettingEntity(HospitalSettingKeys.CURRENCY_SYMBOL, "$", "System currency representation", now),
                HospitalSettingEntity(HospitalSettingKeys.IS_DEMO_ENVIRONMENT, "true", "Demo environment active flag", now)
            )
            db.hospitalSettingDao().insertSettings(settings)

            // 2. Departments
            val deptGenMed = DepartmentEntity("dept_gen_med", "GEN", "General Medicine", "Primary outpatient consultations and internal medicine", true)
            val deptCardio = DepartmentEntity("dept_cardio", "CARD", "Cardiology", "Cardiovascular diagnostics and cardiac rehabilitation", true)
            val deptPed = DepartmentEntity("dept_ped", "PED", "Pediatrics", "Infant and adolescent healthcare services", true)
            val deptLab = DepartmentEntity("dept_lab", "LAB", "Pathology & Diagnostics", "Clinical chemistry, hematology, and microbiology", true)
            val deptPharma = DepartmentEntity("dept_pharma", "PHARM", "Central Pharmacy", "Inpatient and outpatient pharmaceuticals dispensing", true)
            val deptEmerg = DepartmentEntity("dept_emerg", "ER", "Emergency & Trauma", "24/7 urgent resuscitation and acute triage", true)

            db.departmentDao().insertDepartments(listOf(deptGenMed, deptCardio, deptPed, deptLab, deptPharma, deptEmerg))

            // 3. Demo Users (all 10 roles seeded with salted password hashes)
            val demoSalt = SecurityManager.generateSalt()
            val demoHash = SecurityManager.hashPassword("Password123!", demoSalt)

            val users = listOf(
                UserEntity("u_admin", "admin", demoHash, demoSalt, "Dr. Harold Finch", UserRole.HOSPITAL_ADMIN, null, "admin@careflow.health", "+15551000001", true, 0, null, false, now, now),
                UserEntity("u_doc_carter", "doctor.carter", demoHash, demoSalt, "Dr. Maya Carter", UserRole.DOCTOR, "dept_cardio", "m.carter@careflow.health", "+15551000002", true, 0, null, false, now, now),
                UserEntity("u_doc_smith", "doctor.smith", demoHash, demoSalt, "Dr. Alan Smith", UserRole.DOCTOR, "dept_gen_med", "a.smith@careflow.health", "+15551000003", true, 0, null, false, now, now),
                UserEntity("u_nurse_clara", "nurse.clara", demoHash, demoSalt, "Clara Higgins, RN", UserRole.NURSE, "dept_gen_med", "c.higgins@careflow.health", "+15551000004", true, 0, null, false, now, now),
                UserEntity("u_reception", "reception.ann", demoHash, demoSalt, "Ann Davies", UserRole.RECEPTIONIST, null, "a.davies@careflow.health", "+15551000005", true, 0, null, false, now, now),
                UserEntity("u_queue_op", "queue.operator", demoHash, demoSalt, "Marcus Lee", UserRole.QUEUE_OPERATOR, null, "m.lee@careflow.health", "+15551000006", true, 0, null, false, now, now),
                UserEntity("u_lab_tech", "lab.tech", demoHash, demoSalt, "Jordan Vance", UserRole.LAB_TECHNICIAN, "dept_lab", "j.vance@careflow.health", "+15551000007", true, 0, null, false, now, now),
                UserEntity("u_pharma", "pharma.alex", demoHash, demoSalt, "Alex Morgan, RPh", UserRole.PHARMACIST, "dept_pharma", "a.morgan@careflow.health", "+15551000008", true, 0, null, false, now, now),
                UserEntity("u_cashier", "cashier.david", demoHash, demoSalt, "David Miller", UserRole.CASHIER, null, "d.miller@careflow.health", "+15551000009", true, 0, null, false, now, now),
                UserEntity("u_patient", "patient.john", demoHash, demoSalt, "Johnathan Doe", UserRole.PATIENT, null, "j.doe@example.com", "+15551000010", true, 0, null, false, now, now),
                UserEntity("u_super_admin", "super.admin", demoHash, demoSalt, "System Administrator", UserRole.SUPER_ADMIN, null, "root@careflow.health", "+15551000099", true, 0, null, false, now, now)
            )
            db.userDao().insertUsers(users)

            // 4. Doctors
            val doctors = listOf(
                DoctorEntity("doc_carter", "u_doc_carter", "Dr. Maya Carter", "Cardiology Specialist", "dept_cardio", "MD-CARD-8831", "Mon, Tue, Wed, Fri", "08:00 - 16:00", true),
                DoctorEntity("doc_smith", "u_doc_smith", "Dr. Alan Smith", "General Physician", "dept_gen_med", "MD-GEN-4912", "Mon, Tue, Thu, Sat", "09:00 - 17:00", true)
            )
            db.doctorDao().insertDoctors(doctors)

            // 5. Patients with rich Phase 2 attributes
            val patients = listOf(
                PatientEntity(
                    id = "pat_001",
                    hospitalRegNo = "CF-2026-000001",
                    fullName = "Johnathan Doe",
                    dob = "1985-06-14",
                    gender = "MALE",
                    nationalIdOrPassport = "NIC-851654321V",
                    phone = "+15551000010",
                    email = "j.doe@example.com",
                    address = "42 Elm Street, Suite 3, Metro City",
                    preferredLanguage = "English",
                    bloodGroup = "O+",
                    allergies = "Penicillin",
                    existingConditions = "Hypertension",
                    emergencyContactName = "Sarah Doe",
                    emergencyContactRelationship = "Spouse",
                    emergencyContactPhone = "+15551000011",
                    emergencyContactSecondaryPhone = "+15551000091",
                    registeredDate = now - 86400000L * 15,
                    status = "ACTIVE",
                    createdAt = now - 86400000L * 15,
                    updatedAt = now - 86400000L * 15,
                    createdBy = "reception.ann",
                    updatedBy = "reception.ann"
                ),
                PatientEntity(
                    id = "pat_002",
                    hospitalRegNo = "CF-2026-000002",
                    fullName = "Eleanor Vance",
                    dob = "1992-11-23",
                    gender = "FEMALE",
                    nationalIdOrPassport = "PASSPORT-US992817",
                    phone = "+15551000012",
                    email = "e.vance@example.com",
                    address = "108 Oakwood Avenue, Apartment 4B",
                    preferredLanguage = "English",
                    bloodGroup = "A+",
                    allergies = "None known",
                    existingConditions = "Mild Asthma",
                    emergencyContactName = "Thomas Vance",
                    emergencyContactRelationship = "Father",
                    emergencyContactPhone = "+15551000013",
                    emergencyContactSecondaryPhone = "",
                    registeredDate = now - 86400000L * 5,
                    status = "ACTIVE",
                    createdAt = now - 86400000L * 5,
                    updatedAt = now - 86400000L * 5,
                    createdBy = "reception.ann",
                    updatedBy = "reception.ann"
                ),
                PatientEntity(
                    id = "pat_003",
                    hospitalRegNo = "CF-2026-000003",
                    fullName = "Arthur Pendelton",
                    dob = "1960-03-08",
                    gender = "MALE",
                    nationalIdOrPassport = "NIC-600681249V",
                    phone = "+15551000014",
                    email = "a.pendelton@example.com",
                    address = "17 Pinecrest Drive, High Hills",
                    preferredLanguage = "English",
                    bloodGroup = "B+",
                    allergies = "Sulfa drugs",
                    existingConditions = "Type 2 Diabetes",
                    emergencyContactName = "Martha Pendelton",
                    emergencyContactRelationship = "Spouse",
                    emergencyContactPhone = "+15551000015",
                    emergencyContactSecondaryPhone = "",
                    registeredDate = now - 86400000L * 2,
                    status = "ACTIVE",
                    createdAt = now - 86400000L * 2,
                    updatedAt = now - 86400000L * 2,
                    createdBy = "reception.ann",
                    updatedBy = "reception.ann"
                ),
                PatientEntity(
                    id = "pat_004",
                    hospitalRegNo = "CF-2026-000004",
                    fullName = "Sophia Isabella Martinez",
                    dob = "2001-08-19",
                    gender = "FEMALE",
                    nationalIdOrPassport = "PASSPORT-E8172901",
                    phone = "+15551000016",
                    email = "sophia.martinez@example.com",
                    address = "902 Sunset Boulevard, West District",
                    preferredLanguage = "Spanish",
                    bloodGroup = "AB+",
                    allergies = "Latex, Peanut",
                    existingConditions = "None",
                    emergencyContactName = "Carlos Martinez",
                    emergencyContactRelationship = "Brother",
                    emergencyContactPhone = "+15551000017",
                    emergencyContactSecondaryPhone = "",
                    registeredDate = now - 86400000L * 30,
                    status = "INACTIVE",
                    createdAt = now - 86400000L * 30,
                    updatedAt = now - 86400000L * 10,
                    createdBy = "reception.ann",
                    updatedBy = "admin"
                ),
                PatientEntity(
                    id = "pat_005",
                    hospitalRegNo = "CF-2026-000005",
                    fullName = "Robert James Sterling",
                    dob = "1948-12-02",
                    gender = "MALE",
                    nationalIdOrPassport = "NIC-483371902V",
                    phone = "+15551000018",
                    email = "r.sterling@example.com",
                    address = "33 Lakeview Terrace",
                    preferredLanguage = "English",
                    bloodGroup = "O-",
                    allergies = "Aspirin",
                    existingConditions = "Coronary Artery Disease",
                    emergencyContactName = "David Sterling",
                    emergencyContactRelationship = "Son",
                    emergencyContactPhone = "+15551000019",
                    emergencyContactSecondaryPhone = "",
                    registeredDate = now - 86400000L * 90,
                    status = "ARCHIVED",
                    createdAt = now - 86400000L * 90,
                    updatedAt = now - 86400000L * 1,
                    createdBy = "admin",
                    updatedBy = "admin"
                )
            )
            db.patientDao().insertPatients(patients)

            // Primary Emergency Contacts
            val emergencyContacts = listOf(
                EmergencyContactEntity("ec_001", "pat_001", "Sarah Doe", "Spouse", "+15551000011", "+15551000091", "42 Elm Street, Suite 3", true, now, now),
                EmergencyContactEntity("ec_002", "pat_002", "Thomas Vance", "Father", "+15551000013", "", "108 Oakwood Avenue", true, now, now),
                EmergencyContactEntity("ec_003", "pat_003", "Martha Pendelton", "Spouse", "+15551000015", "", "17 Pinecrest Drive", true, now, now),
                EmergencyContactEntity("ec_004", "pat_004", "Carlos Martinez", "Brother", "+15551000017", "", "902 Sunset Boulevard", true, now, now),
                EmergencyContactEntity("ec_005", "pat_005", "David Sterling", "Son", "+15551000019", "", "33 Lakeview Terrace", true, now, now)
            )
            db.emergencyContactDao().insertContacts(emergencyContacts)

            // 6. Appointments
            val appointments = listOf(
                AppointmentEntity("apt_001", "pat_001", "doc_carter", "dept_cardio", now + 3600000L * 2, "CONSULTATION", "CONFIRMED", "Routine cardiac checkup and blood pressure assessment"),
                AppointmentEntity("apt_002", "pat_002", "doc_smith", "dept_gen_med", now + 3600000L * 4, "FOLLOW_UP", "CONFIRMED", "Asthma inhaler review and spirometry follow-up"),
                AppointmentEntity("apt_003", "pat_003", "doc_smith", "dept_gen_med", now - 86400000L, "CONSULTATION", "COMPLETED", "Blood sugar management consultation")
            )
            db.appointmentDao().insertAppointments(appointments)

            // 7. Queue Tickets
            val queueTickets = listOf(
                QueueTicketEntity(
                    id = "q_001",
                    ticketNumber = "A-101",
                    patientId = "pat_001",
                    departmentId = "dept_cardio",
                    doctorId = "doc_carter",
                    appointmentId = "apt_001",
                    priority = 0,
                    status = "WAITING",
                    estimatedWaitMinutes = 15,
                    checkInTime = now - 600000L
                ),
                QueueTicketEntity(
                    id = "q_002",
                    ticketNumber = "A-102",
                    patientId = "pat_002",
                    departmentId = "dept_gen_med",
                    doctorId = "doc_smith",
                    appointmentId = "apt_002",
                    priority = 1,
                    status = "WAITING",
                    estimatedWaitMinutes = 25,
                    checkInTime = now - 300000L
                )
            )
            db.queueDao().insertTickets(queueTickets)

            // 8. Vital Signs
            val vitals = VitalSignEntity(
                id = "vit_001",
                patientId = "pat_001",
                recordedByUserId = "u_nurse_clara",
                timestamp = now - 1800000L,
                temperatureCelsius = 36.8f,
                systolicBp = 128,
                diastolicBp = 82,
                pulseRateBpm = 72,
                respiratoryRateBpm = 16,
                oxygenSaturationPercent = 99,
                weightKg = 76.5f,
                heightCm = 178f,
                painScore = 1,
                urgentAttentionFlag = false,
                triageNotes = "Patient resting comfortably. Normal baseline vitals."
            )
            db.clinicalDao().insertVitals(vitals)

            // 9. Initial Audit Log
            val initLog = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                userId = "u_admin",
                username = "admin",
                userRole = UserRole.HOSPITAL_ADMIN,
                action = AuditAction.SETTINGS_UPDATED,
                entityName = "Database",
                entityId = "careflow_hospital.db",
                details = "CareFlow hospital database initialized in DEMO mode with standard roles and operational seed data.",
                ipOrDevice = "Android Hospital Terminal",
                result = "SUCCESS"
            )
            db.auditLogDao().insertLog(initLog)
        }
    }
}
