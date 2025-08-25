package dev.achmad.checkin.domain.repository

import dev.achmad.checkin.domain.model.User

interface UserRepository {
    suspend fun getUserById(userId: String): User
    suspend fun getCurrentUser(): User
}