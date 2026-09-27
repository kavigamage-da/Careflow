package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.example.di.LocalAppContainer
import com.example.ui.navigation.AppNavHost
import com.example.ui.theme.CareFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as CareFlowApplication).container

        setContent {
            CompositionLocalProvider(LocalAppContainer provides appContainer) {
                CareFlowTheme {
                    AppNavHost()
                }
            }
        }
    }
}
