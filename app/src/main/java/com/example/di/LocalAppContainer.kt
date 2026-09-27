package com.example.di

import androidx.compose.runtime.compositionLocalOf

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("No AppContainer provided in CompositionLocal")
}
