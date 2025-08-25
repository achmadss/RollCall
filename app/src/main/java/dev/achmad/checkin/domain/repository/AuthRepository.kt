package dev.achmad.checkin.domain.repository

import dev.achmad.checkin.domain.model.SignInOption

interface AuthRepository {
    suspend fun signIn(option: SignInOption): Token
    suspend fun signOut()
}

typealias Token = String