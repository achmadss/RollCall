package dev.achmad.rollcall.core.di

import dev.achmad.rollcall.core.network.NetworkHelper
import dev.achmad.rollcall.core.preference.AndroidPreferenceStore
import dev.achmad.rollcall.core.preference.PreferenceStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { NetworkHelper(androidContext(), false) }
    single<PreferenceStore> { AndroidPreferenceStore(androidContext()) }
}