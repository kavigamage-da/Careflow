package com.example.core.model

enum class QueuePriority(
    val level: Int,
    val displayName: String,
    val description: String,
    val requiresClinicalAuthorization: Boolean
) {
    STANDARD(0, "Standard", "Routine outpatient visit", false),
    FAST_TRACK(1, "Fast Track", "Expedited for elderly, infants, or brief review", false),
    URGENT(2, "Urgent", "Acute symptoms requiring prompt attention", false),
    EMERGENCY(3, "Emergency", "Immediate life or organ threat; requires triage/clinical authorization", true);

    companion object {
        fun fromLevel(level: Int): QueuePriority {
            return values().firstOrNull { it.level == level } ?: STANDARD
        }

        fun fromString(value: String?): QueuePriority {
            if (value == null) return STANDARD
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                fromLevel(value.toIntOrNull() ?: 0)
            }
        }
    }
}
