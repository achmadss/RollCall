package dev.achmad.checkin.core.di

import dev.achmad.checkin.presentation.util.ToastHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val presentationModule = module {
    single { ToastHelper(androidContext()) }
}