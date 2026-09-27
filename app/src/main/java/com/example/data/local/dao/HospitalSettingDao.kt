package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.HospitalSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalSettingDao {

    @Query("SELECT * FROM hospital_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): HospitalSettingEntity?

    @Query("SELECT * FROM hospital_settings")
    fun getAllSettings(): Flow<List<HospitalSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: HospitalSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<HospitalSettingEntity>)

    @Query("UPDATE hospital_settings SET value = :value, updated_at = :updatedAt WHERE `key` = :key")
    suspend fun updateSetting(key: String, value: String, updatedAt: Long = System.currentTimeMillis())
}
