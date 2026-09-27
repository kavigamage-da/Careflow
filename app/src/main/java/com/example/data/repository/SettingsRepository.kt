package com.example.data.repository

import com.example.core.model.HospitalSetting
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAllSettings(): Flow<List<HospitalSetting>>
    suspend fun getSettingValue(key: String): String?
    suspend fun updateSetting(key: String, value: String)
}
