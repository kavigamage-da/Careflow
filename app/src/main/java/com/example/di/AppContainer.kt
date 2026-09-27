package com.example.di

import android.content.Context
import com.example.core.security.SessionManager
import com.example.data.local.CareFlowDatabase
import com.example.data.repository.AuditRepository
import com.example.data.repository.AuditRepositoryImpl
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.BillingRepository
import com.example.data.repository.BillingRepositoryImpl
import com.example.data.repository.ClinicalRepository
import com.example.data.repository.ClinicalRepositoryImpl
import com.example.data.repository.LabRepository
import com.example.data.repository.LabRepositoryImpl
import com.example.data.repository.PatientRepository
import com.example.data.repository.PatientRepositoryImpl
import com.example.data.repository.PharmacyRepository
import com.example.data.repository.PharmacyRepositoryImpl
import com.example.data.repository.QueueRepository
import com.example.data.repository.QueueRepositoryImpl
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SettingsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface AppContainer {
    val database: CareFlowDatabase
    val sessionManager: SessionManager
    val authRepository: AuthRepository
    val auditRepository: AuditRepository
    val settingsRepository: SettingsRepository
    val patientRepository: PatientRepository
    val queueRepository: QueueRepository
    val clinicalRepository: ClinicalRepository
    val pharmacyRepository: PharmacyRepository
    val labRepository: LabRepository
    val billingRepository: BillingRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val sessionManager: SessionManager by lazy {
        SessionManager()
    }

    override val database: CareFlowDatabase by lazy {
        CareFlowDatabase.getInstance(context, applicationScope)
    }

    override val auditRepository: AuditRepository by lazy {
        AuditRepositoryImpl(
            auditLogDao = database.auditLogDao(),
            sessionManager = sessionManager
        )
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(
            settingDao = database.hospitalSettingDao(),
            auditRepository = auditRepository
        )
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            userDao = database.userDao(),
            sessionManager = sessionManager,
            auditRepository = auditRepository
        )
    }

    override val patientRepository: PatientRepository by lazy {
        PatientRepositoryImpl(
            database = database,
            sessionManager = sessionManager,
            auditRepository = auditRepository
        )
    }

    override val queueRepository: QueueRepository by lazy {
        QueueRepositoryImpl(
            database = database,
            sessionManager = sessionManager,
            auditRepository = auditRepository
        )
    }

    override val clinicalRepository: ClinicalRepository by lazy {
        ClinicalRepositoryImpl(
            database = database,
            sessionManager = sessionManager,
            auditRepository = auditRepository
        )
    }

    override val pharmacyRepository: PharmacyRepository by lazy {
        PharmacyRepositoryImpl(
            database = database,
            sessionManager = sessionManager,
            auditRepository = auditRepository
        )
    }

    override val labRepository: LabRepository by lazy {
        LabRepositoryImpl(
            database = database,
            sessionManager = sessionManager,
            auditRepository = auditRepository
        )
    }

    override val billingRepository: BillingRepository by lazy {
        BillingRepositoryImpl(
            database = database,
            sessionManager = sessionManager,
            auditRepository = auditRepository
        )
    }
}
