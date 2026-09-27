package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.core.model.AuthUser

@Composable
fun CareFlowScaffold(
    title: String,
    currentUser: AuthUser? = null,
    canNavigateBack: Boolean = false,
    showDemoBanner: Boolean = true,
    onNavigateBack: () -> Unit = {},
    onNavigateAuditLogs: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                if (showDemoBanner) {
                    DemoEnvironmentBanner()
                }
                CareFlowTopBar(
                    title = title,
                    currentUser = currentUser,
                    canNavigateBack = canNavigateBack,
                    onNavigateBack = onNavigateBack,
                    onNavigateAuditLogs = onNavigateAuditLogs,
                    onNavigateSettings = onNavigateSettings,
                    onLogout = onLogout
                )
            }
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content(innerPadding)
        }
    }
}
