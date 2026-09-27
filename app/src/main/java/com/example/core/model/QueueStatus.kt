package com.example.core.model

enum class QueueStatus(val displayName: String, val description: String) {
    WAITING("Waiting", "Patient is in the waiting area awaiting call"),
    CALLED("Called", "Patient has been paged and directed to consultation room"),
    IN_CONSULTATION("In Consultation", "Doctor has commenced clinical consultation"),
    COMPLETED("Completed", "Consultation concluded and patient released"),
    SKIPPED("Skipped", "Patient did not respond to multiple calls"),
    CANCELLED("Cancelled", "Ticket cancelled by staff or patient departure");

    fun canTransitionTo(target: QueueStatus): Boolean {
        if (this == target) {
            // Self-transition is only valid for CALLED (recalling patient)
            return this == CALLED
        }

        return when (this) {
            WAITING -> target in setOf(CALLED, SKIPPED, CANCELLED)
            CALLED -> target in setOf(IN_CONSULTATION, SKIPPED, CANCELLED)
            IN_CONSULTATION -> target in setOf(COMPLETED)
            COMPLETED, SKIPPED, CANCELLED -> false // Terminal states
        }
    }

    companion object {
        fun validateTransition(current: QueueStatus, target: QueueStatus): Result<Unit> {
            return if (current.canTransitionTo(target)) {
                Result.success(Unit)
            } else {
                Result.failure(
                    IllegalStateException("Invalid queue state transition: cannot transition from ${current.name} to ${target.name}")
                )
            }
        }
    }
}
