package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.model.UserRole
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role AND is_active = 1")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY full_name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET failed_login_attempts = :attempts, locked_until = :lockedUntil WHERE id = :id")
    suspend fun updateLockoutState(id: String, attempts: Int, lockedUntil: Long?)

    @Query("UPDATE users SET last_login_at = :lastLoginAt, failed_login_attempts = 0, locked_until = NULL WHERE id = :id")
    suspend fun recordSuccessfulLogin(id: String, lastLoginAt: Long)

    @Query("UPDATE users SET password_hash = :hash, salt = :salt, force_password_change = 0 WHERE id = :id")
    suspend fun updatePassword(id: String, hash: String, salt: String)
}
