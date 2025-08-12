package dev.achmad.rollcall.core.di

import dev.achmad.rollcall.domain.preference.AuthPreference
import dev.achmad.rollcall.ui.util.ToastHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val uiModule = module {
    single { ToastHelper(androidContext()) }
    single { AuthPreference(get()) }
}