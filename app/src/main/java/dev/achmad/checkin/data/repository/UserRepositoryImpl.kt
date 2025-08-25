package dev.achmad.checkin.data.repository

import dev.achmad.checkin.core.network.GET
import dev.achmad.checkin.core.network.NetworkHelper
import dev.achmad.checkin.core.network.awaitBaseResponse
import dev.achmad.checkin.data.remote.model.base.NullDataResponseException
import dev.achmad.checkin.domain.API_URL_V1
import dev.achmad.checkin.domain.model.User
import dev.achmad.checkin.domain.repository.UserRepository

class UserRepositoryImpl(
    private val networkHelper: NetworkHelper,
): UserRepository {

    override suspend fun getUserById(userId: String): User {
        return networkHelper.client.newCall(
            GET("$API_URL_V1/user/$userId")
        ).awaitBaseResponse<User>().data
            ?: throw NullDataResponseException()
    }

    override suspend fun getCurrentUser(): User {
        return networkHelper.client.newCall(
            GET("$API_URL_V1/me")
        ).awaitBaseResponse<User>().data
            ?: throw NullDataResponseException()
    }

}