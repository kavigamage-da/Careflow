package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "doctors",
    foreignKeys = [
        ForeignKey(
            entity = DepartmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["department_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("department_id"),
        Index("user_id")
    ]
)
data class DoctorEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String?,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "specialty")
    val specialty: String,

    @ColumnInfo(name = "department_id")
    val departmentId: String?,

    @ColumnInfo(name = "license_number")
    val licenseNumber: String,

    @ColumnInfo(name = "working_days")
    val workingDays: String,

    @ColumnInfo(name = "working_hours")
    val workingHours: String,

    @ColumnInfo(name = "is_available")
    val isAvailable: Boolean = true
)
