package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patients",
    indices = [
        Index(value = ["hospital_reg_no"], unique = true),
        Index("full_name"),
        Index("phone"),
        Index("email"),
        Index("national_id_or_passport"),
        Index("status")
    ]
)
data class PatientEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "hospital_reg_no")
    val hospitalRegNo: String,

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "dob")
    val dob: String, // YYYY-MM-DD

    @ColumnInfo(name = "gender")
    val gender: String, // "MALE", "FEMALE", "OTHER"

    @ColumnInfo(name = "national_id_or_passport")
    val nationalIdOrPassport: String = "",

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "address")
    val address: String = "",

    @ColumnInfo(name = "preferred_language")
    val preferredLanguage: String = "English",

    @ColumnInfo(name = "blood_group")
    val bloodGroup: String, // "O+", "A+", "B+", etc.

    @ColumnInfo(name = "allergies")
    val allergies: String,

    @ColumnInfo(name = "existing_conditions")
    val existingConditions: String,

    @ColumnInfo(name = "emergency_contact_name")
    val emergencyContactName: String,

    @ColumnInfo(name = "emergency_contact_relationship")
    val emergencyContactRelationship: String = "Family",

    @ColumnInfo(name = "emergency_contact_phone")
    val emergencyContactPhone: String,

    @ColumnInfo(name = "emergency_contact_secondary_phone")
    val emergencyContactSecondaryPhone: String = "",

    @ColumnInfo(name = "registered_date")
    val registeredDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "status")
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE", "DECEASED", "TRANSFERRED", "ARCHIVED"

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "created_by")
    val createdBy: String = "",

    @ColumnInfo(name = "updated_by")
    val updatedBy: String = ""
)
