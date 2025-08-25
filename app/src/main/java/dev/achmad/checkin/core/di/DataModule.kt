package dev.achmad.checkin.core.di

import dev.achmad.checkin.data.repository.AttendanceRepositoryImpl
import dev.achmad.checkin.data.repository.AuthRepositoryImpl
import dev.achmad.checkin.data.repository.CompanyRepositoryImpl
import dev.achmad.checkin.data.repository.UserRepositoryImpl
import dev.achmad.checkin.domain.repository.AttendanceRepository
import dev.achmad.checkin.domain.repository.AuthRepository
import dev.achmad.checkin.domain.repository.CompanyRepository
import dev.achmad.checkin.domain.repository.UserRepository
import org.koin.dsl.module

val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<AttendanceRepository> { AttendanceRepositoryImpl(get()) }
    single<CompanyRepository> { CompanyRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
}