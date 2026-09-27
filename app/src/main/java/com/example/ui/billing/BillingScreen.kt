package com.example.ui.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.util.DateTimeUtils
import com.example.data.local.entity.InvoiceEntity
import com.example.ui.components.CareFlowScaffold
import com.example.ui.components.StatusChip

@Composable
fun BillingScreen(
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateAuditLogs: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    CareFlowScaffold(
        title = "Cashier & Billing",
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        onNavigateAuditLogs = onNavigateAuditLogs,
        onNavigateSettings = onNavigateSettings
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Revenue Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Hospital Revenue",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$${String.format("%.2f", uiState.totalRevenue)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Pending Invoices",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${uiState.pendingInvoices.size} bills",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Pending (${uiState.pendingInvoices.size})") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("All Transactions (${uiState.allInvoices.size})") }
                )
            }

            val list = if (uiState.selectedTab == 0) uiState.pendingInvoices else uiState.allInvoices

            if (uiState.isLoading && list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.selectedTab == 0) "No outstanding unpaid bills" else "No invoices recorded",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.id }) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            onPay = { viewModel.selectInvoiceForPayment(invoice) },
                            onViewReceipt = { viewModel.viewReceipt(invoice) }
                        )
                    }
                }
            }
        }
    }

    // Payment Processing Dialog
    uiState.invoiceForPayment?.let { invoice ->
        PaymentDialog(
            invoice = invoice,
            onDismiss = { viewModel.selectInvoiceForPayment(null) },
            onConfirm = { amount, method ->
                viewModel.processPayment(invoice.id, amount, method)
            }
        )
    }

    // Receipt View Dialog
    uiState.receiptToView?.let { invoice ->
        ReceiptDialog(
            invoice = invoice,
            onDismiss = { viewModel.viewReceipt(null) }
        )
    }
}

@Composable
fun InvoiceCard(
    invoice: InvoiceEntity,
    onPay: () -> Unit,
    onViewReceipt: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = invoice.invoiceNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Patient ID: ${invoice.patientId} • ${DateTimeUtils.formatDateTime(invoice.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Charges Breakdown
            Text(
                text = "Services: ${invoice.itemsJson}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total: $${String.format("%.2f", invoice.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (invoice.paidAmount > 0.0) {
                        Text(
                            text = "Paid: $${String.format("%.2f", invoice.paidAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                if (invoice.status == "PENDING" || invoice.status == "PARTIALLY_PAID") {
                    Button(
                        onClick = onPay,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay Bill")
                    }
                } else {
                    OutlinedButton(onClick = onViewReceipt) {
                        Text("View Receipt")
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    val remainingBalance = invoice.totalAmount - invoice.paidAmount
    var amountText by remember { mutableStateOf(String.format("%.2f", remainingBalance)) }
    var selectedMethod by remember { mutableStateOf("CASH") }

    val methods = listOf("CASH" to "Cash", "CARD" to "Card / POS", "INSURANCE" to "Insurance")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Process Bill Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Invoice: ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold)
                Text("Outstanding Balance: $${String.format("%.2f", remainingBalance)}")

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount ($)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Payment Method", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.forEach { (id, label) ->
                        FilterChip(
                            selected = selectedMethod == id,
                            onClick = { selectedMethod = id },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: remainingBalance
                    onConfirm(amount, selectedMethod)
                }
            ) {
                Text("Process Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReceiptDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("CareFlow Hospital", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Payment Receipt (Demonstration)", style = MaterialTheme.typography.bodySmall)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Receipt #:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Patient ID:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(invoice.patientId)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payment Date:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(invoice.paidAt?.let { DateTimeUtils.formatDateTime(it) } ?: DateTimeUtils.formatDateTime(invoice.createdAt))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Method:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(invoice.paymentMethod ?: "CASH", fontWeight = FontWeight.SemiBold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Items & Breakdown:", fontWeight = FontWeight.SemiBold)
                Text(invoice.itemsJson, style = MaterialTheme.typography.bodySmall)

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount:", fontWeight = FontWeight.Bold)
                    Text("$${String.format("%.2f", invoice.totalAmount)}", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Amount Paid:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text("$${String.format("%.2f", invoice.paidAmount)}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close Receipt") }
        }
    )
}
