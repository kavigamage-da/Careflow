package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.model.HospitalSetting
import com.example.data.local.dao.HospitalSettingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val settingDao: HospitalSettingDao,
    private val auditRepository: AuditRepository
) : SettingsRepository {

    override fun getAllSettings(): Flow<List<HospitalSetting>> {
        return settingDao.getAllSettings().map { entities ->
            entities.map { HospitalSetting(it.key, it.value, it.description, it.updatedAt) }
        }
    }

    override suspend fun getSettingValue(key: String): String? {
        return settingDao.getSetting(key)?.value
    }

    override suspend fun updateSetting(key: String, value: String) {
        settingDao.updateSetting(key, value)
        auditRepository.recordAction(
            action = AuditAction.SETTINGS_UPDATED,
            entityName = "HospitalSetting",
            entityId = key,
            details = "Updated configuration key '$key' to '$value'",
            result = "SUCCESS"
        )
    }
}
