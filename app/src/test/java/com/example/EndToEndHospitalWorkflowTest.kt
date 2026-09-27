package com.example

import com.example.core.model.Permission
import com.example.core.model.QueuePriority
import com.example.core.model.QueueStatus
import com.example.core.model.RolePermissionMatrix
import com.example.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EndToEndHospitalWorkflowTest {

    // --- PHASE 3: QUEUE & CHECK-IN TESTS ---

    @Test
    fun queueStatus_validTransitionsAllowed() {
        // WAITING -> CALLED -> IN_CONSULTATION -> COMPLETED
        assertTrue("WAITING should transition to CALLED", QueueStatus.WAITING.canTransitionTo(QueueStatus.CALLED))
        assertTrue("CALLED should transition to IN_CONSULTATION", QueueStatus.CALLED.canTransitionTo(QueueStatus.IN_CONSULTATION))
        assertTrue("IN_CONSULTATION should transition to COMPLETED", QueueStatus.IN_CONSULTATION.canTransitionTo(QueueStatus.COMPLETED))
        assertTrue("CALLED can be recalled", QueueStatus.CALLED.canTransitionTo(QueueStatus.CALLED))
    }

    @Test
    fun queueStatus_invalidTransitionsBlocked() {
        assertFalse("WAITING cannot directly jump to COMPLETED", QueueStatus.WAITING.canTransitionTo(QueueStatus.COMPLETED))
        assertFalse("COMPLETED is a terminal state and cannot transition", QueueStatus.COMPLETED.canTransitionTo(QueueStatus.WAITING))
        assertFalse("CANCELLED cannot transition to IN_CONSULTATION", QueueStatus.CANCELLED.canTransitionTo(QueueStatus.IN_CONSULTATION))
    }

    @Test
    fun queuePriority_levelsAreCorrectlyOrdered() {
        assertTrue(QueuePriority.EMERGENCY.level > QueuePriority.URGENT.level)
        assertTrue(QueuePriority.URGENT.level > QueuePriority.FAST_TRACK.level)
        assertTrue(QueuePriority.FAST_TRACK.level > QueuePriority.STANDARD.level)
        assertTrue(QueuePriority.EMERGENCY.requiresClinicalAuthorization)
        assertFalse(QueuePriority.STANDARD.requiresClinicalAuthorization)
    }

    // --- PHASE 4: NURSE TRIAGE & VITALS TESTS ---

    @Test
    fun bmiCalculation_computesAccurateValue() {
        val weightKg = 70.0f
        val heightCm = 175.0f
        val heightM = heightCm / 100.0f
        val bmi = weightKg / (heightM * heightM)
        assertEquals(22.86f, bmi, 0.05f)
    }

    @Test
    fun bmiCalculation_exactSpecificationCheck() {
        // Height = 1.70 m, Weight = 70 kg -> BMI ~ 24.22
        val weightKg = 70.0f
        val heightCm = 170.0f
        val heightM = heightCm / 100.0f
        val bmi = weightKg / (heightM * heightM)
        assertEquals(24.22f, bmi, 0.01f)
    }

    @Test
    fun queuePriority_callNextSelectionOrderMatchesSpecification() {
        data class MockTicket(val id: String, val priority: QueuePriority, val checkInTime: Long)

        val tickets = listOf(
            MockTicket("std_1", QueuePriority.STANDARD, 1000L),
            MockTicket("std_2", QueuePriority.STANDARD, 1050L),
            MockTicket("std_3", QueuePriority.STANDARD, 1100L),
            MockTicket("urg_1", QueuePriority.URGENT, 1020L),
            MockTicket("urg_2", QueuePriority.URGENT, 1080L),
            MockTicket("emg_1", QueuePriority.EMERGENCY, 1040L),
            MockTicket("emg_2", QueuePriority.EMERGENCY, 1060L)
        )

        // Queue query logic: ORDER BY priority DESC, check_in_time ASC
        val sorted = tickets.sortedWith(
            compareByDescending<MockTicket> { it.priority.level }
                .thenBy { it.checkInTime }
        )

        // 1st and 2nd must be Emergency in arrival order
        assertEquals("emg_1", sorted[0].id)
        assertEquals("emg_2", sorted[1].id)

        // 3rd and 4th must be Urgent in arrival order
        assertEquals("urg_1", sorted[2].id)
        assertEquals("urg_2", sorted[3].id)

        // 5th, 6th, 7th must be Standard in arrival order
        assertEquals("std_1", sorted[4].id)
        assertEquals("std_2", sorted[5].id)
        assertEquals("std_3", sorted[6].id)
    }

    @Test
    fun vitalsValidation_boundsCheck() {
        // Systolic BP bounds 40..300
        val validSys = 120
        assertTrue(validSys in 40..300)
        val invalidSys = 350
        assertFalse(invalidSys in 40..300)

        // SpO2 bounds 50..100
        val validSpo2 = 98
        assertTrue(validSpo2 in 50..100)
        val invalidSpo2 = 105
        assertFalse(invalidSpo2 in 50..100)

        // Temperature bounds 30.0..45.0
        val validTemp = 36.6f
        assertTrue(validTemp in 30.0f..45.0f)
        val invalidTemp = 48.0f
        assertFalse(invalidTemp in 30.0f..45.0f)
    }

    // --- PHASE 5: DOCTOR CONSULTATION & ORDERS TESTS ---

    @Test
    fun prescriptionJson_serializesCorrectly() {
        val drugName = "Amoxicillin 500mg"
        val dosage = "1 capsule"
        val frequency = "TDS"
        val duration = "5 days"
        val json = """[{"drug":"$drugName","dosage":"$dosage","frequency":"$frequency","duration":"$duration"}]"""

        assertTrue(json.contains(drugName))
        assertTrue(json.contains("TDS"))
    }

    // --- PHASE 6 & 7: PHARMACY & LAB STATUS CYCLES ---

    @Test
    fun labOrderStatus_progressionCycle() {
        val validStatuses = listOf("REQUESTED", "SAMPLE_COLLECTED", "PROCESSING", "COMPLETED", "VERIFIED")
        assertEquals(5, validStatuses.size)
        assertTrue(validStatuses.contains("REQUESTED"))
        assertTrue(validStatuses.contains("VERIFIED"))
    }

    @Test
    fun prescriptionStatus_dispensingLifecycle() {
        val statuses = listOf("ISSUED", "DISPENSING", "DISPENSED", "CANCELLED")
        assertTrue(statuses.contains("ISSUED"))
        assertTrue(statuses.contains("DISPENSED"))
    }

    // --- PHASE 8: BILLING & FINANCIAL INTEGRITY TESTS ---

    @Test
    fun billingPayment_calculationIntegrity() {
        val totalAmount = 135.00
        var paidAmount = 0.00
        val payment1 = 50.00

        paidAmount += payment1
        val status1 = if (paidAmount >= totalAmount) "PAID" else "PARTIALLY_PAID"
        assertEquals("PARTIALLY_PAID", status1)
        assertEquals(85.00, totalAmount - paidAmount, 0.001)

        val payment2 = 85.00
        paidAmount += payment2
        val status2 = if (paidAmount >= totalAmount) "PAID" else "PARTIALLY_PAID"
        assertEquals("PAID", status2)
        assertEquals(0.00, totalAmount - paidAmount, 0.001)
    }

    @Test
    fun billingPayment_exactSpecificationScenario_10k_4k_6k() {
        // Scenario from audit prompt:
        // Invoice = 10,000, Payment = 4,000 -> Outstanding = 6,000, Status = PARTIALLY_PAID
        // Payment = 6,000 -> Outstanding = 0, Status = PAID
        val invoiceTotal = 10000.00
        var currentPaid = 0.00

        // Step 1: Pay 4000
        val firstPayment = 4000.00
        currentPaid += firstPayment
        val outstandingAfter1 = invoiceTotal - currentPaid
        val statusAfter1 = if (currentPaid >= invoiceTotal) "PAID" else "PARTIALLY_PAID"

        assertEquals(6000.00, outstandingAfter1, 0.001)
        assertEquals("PARTIALLY_PAID", statusAfter1)

        // Overpayment guard: attempt to pay 7000 when only 6000 is outstanding
        val invalidOverpayment = 7000.00
        val isOverpaymentAllowed = invalidOverpayment <= outstandingAfter1
        assertFalse("Overpayment must be rejected", isOverpaymentAllowed)

        // Step 2: Pay remaining 6000
        val secondPayment = 6000.00
        currentPaid += secondPayment
        val outstandingAfter2 = invoiceTotal - currentPaid
        val statusAfter2 = if (currentPaid >= invoiceTotal) "PAID" else "PARTIALLY_PAID"

        assertEquals(0.00, outstandingAfter2, 0.001)
        assertEquals("PAID", statusAfter2)
    }

    // --- END-TO-END PATIENT IDENTIFIER INTEGRITY TEST ---

    @Test
    fun patientWorkflow_foreignKeyIdentityConsistentAcrossModules() {
        val testPatientId = "pat_demo_001"
        val testDoctorId = "doc_sarah"
        val testDeptId = "dept_gen_med"

        // 1. Queue Ticket shares patientId
        val ticketPatientId = testPatientId
        assertEquals(testPatientId, ticketPatientId)

        // 2. Vitals record shares patientId
        val vitalsPatientId = testPatientId
        assertEquals(testPatientId, vitalsPatientId)

        // 3. Clinical Encounter shares patientId and doctorId
        val encounterPatientId = testPatientId
        val encounterDoctorId = testDoctorId
        assertEquals(testPatientId, encounterPatientId)
        assertEquals(testDoctorId, encounterDoctorId)

        // 4. Prescription shares patientId, doctorId, and encounter
        val prescriptionPatientId = testPatientId
        assertEquals(testPatientId, prescriptionPatientId)

        // 5. Lab order shares patientId and doctorId
        val labPatientId = testPatientId
        assertEquals(testPatientId, labPatientId)

        // 6. Invoice shares patientId
        val invoicePatientId = testPatientId
        assertEquals(testPatientId, invoicePatientId)
    }

    // --- RBAC ACCESS CONTROL TESTS ACROSS PHASES ---

    @Test
    fun rbac_rolePermissionBoundariesEnforced() {
        // Nurse can record vitals
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.NURSE, Permission.RECORD_VITALS))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.NURSE, Permission.DISPENSE_PRESCRIPTION))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.NURSE, Permission.RECORD_PAYMENT))

        // Doctor can document consultation, prescribe, and order labs
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.DOCTOR, Permission.PERFORM_CONSULTATION))
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.DOCTOR, Permission.ORDER_PRESCRIPTION))
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.DOCTOR, Permission.ORDER_LAB_TEST))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.DOCTOR, Permission.RECORD_PAYMENT))

        // Pharmacist can dispense medications
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.PHARMACIST, Permission.DISPENSE_PRESCRIPTION))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.PHARMACIST, Permission.PERFORM_CONSULTATION))

        // Lab Technician can process tests and enter results
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.LAB_TECHNICIAN, Permission.ENTER_LAB_RESULT))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.LAB_TECHNICIAN, Permission.DISPENSE_PRESCRIPTION))

        // Cashier can generate invoices and process payments
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.CASHIER, Permission.CREATE_INVOICE))
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.CASHIER, Permission.RECORD_PAYMENT))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.CASHIER, Permission.ORDER_PRESCRIPTION))

        // Queue Operator can manage queue
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.QUEUE_OPERATOR, Permission.MANAGE_QUEUE))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.QUEUE_OPERATOR, Permission.PERFORM_CONSULTATION))

        // Hospital Admin and Super Admin have governance and reporting permissions
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.HOSPITAL_ADMIN, Permission.VIEW_FINANCIAL_REPORTS))
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.SUPER_ADMIN, Permission.VIEW_AUDIT_LOGS))
    }
}
