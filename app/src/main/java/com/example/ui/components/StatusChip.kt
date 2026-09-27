package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusCalled
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusUrgent
import com.example.ui.theme.StatusWaiting

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "WAITING", "REQUESTED", "PENDING" -> Color(0xFFFFF3E0) to StatusWaiting
        "CALLED", "PROCESSING", "DISPENSING" -> Color(0xFFE1F5FE) to StatusCalled
        "ACTIVE", "CONFIRMED", "COMPLETED", "VERIFIED", "PAID" -> Color(0xFFE8F5E9) to StatusActive
        "URGENT", "EMERGENCY", "LOCKED", "CANCELLED" -> Color(0xFFFFEBEE) to StatusUrgent
        else -> Color(0xFFECEFF1) to StatusCompleted
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.replace("_", " ").uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
