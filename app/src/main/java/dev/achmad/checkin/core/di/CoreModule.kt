package dev.achmad.checkin.core.di

import android.content.Context
import dev.achmad.checkin.core.network.NetworkHelper
import dev.achmad.checkin.core.preference.AndroidPreferenceStore
import dev.achmad.checkin.core.preference.PreferenceStore
import dev.achmad.checkin.domain.SHARED_PREFERENCES_NAME
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { NetworkHelper(androidContext(), true) }
    single<PreferenceStore> {
        AndroidPreferenceStore(
            androidContext().getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
            )
        )
    }
}