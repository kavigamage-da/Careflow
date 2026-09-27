package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index("recipient_user_id"),
        Index("is_read"),
        Index("created_at")
    ]
)
data class NotificationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "recipient_user_id")
    val recipientUserId: String,

    @ColumnInfo(name = "type")
    val type: String, // "APPOINTMENT_REMINDER", "QUEUE_CALLED", "LAB_READY", "PRESCRIPTION_READY", "SYSTEM_ALERT"

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "related_entity_id")
    val relatedEntityId: String? = null,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
