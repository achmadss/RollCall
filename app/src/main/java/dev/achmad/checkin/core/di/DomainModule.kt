package dev.achmad.checkin.core.di

import dev.achmad.checkin.domain.preference.AuthPreference
import org.koin.dsl.module

val domainModule = module {
    single { AuthPreference(get()) }
}