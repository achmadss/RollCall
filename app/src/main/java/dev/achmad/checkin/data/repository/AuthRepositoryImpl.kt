package dev.achmad.checkin.data.repository

import dev.achmad.checkin.core.network.NetworkHelper
import dev.achmad.checkin.core.network.POST
import dev.achmad.checkin.core.network.awaitBaseResponse
import dev.achmad.checkin.data.remote.model.base.NullDataResponseException
import dev.achmad.checkin.data.remote.model.signin.SignInRequest
import dev.achmad.checkin.data.remote.model.signin.SignInResponse
import dev.achmad.checkin.domain.API_URL_V1
import dev.achmad.checkin.domain.model.SignInOption
import dev.achmad.checkin.domain.preference.AuthPreference
import dev.achmad.checkin.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val networkHelper: NetworkHelper,
    private val authPreference: AuthPreference,
): AuthRepository {

    override suspend fun signIn(option: SignInOption): String {
        return when(option) {
            is SignInOption.Basic -> {
                val token = networkHelper.client.newCall(
                    POST(
                        url = "$API_URL_V1/sign-in",
                        body = SignInRequest.Basic(
                            username = option.username,
                            password = option.password
                        ).toRequestBody()
                    )
                ).awaitBaseResponse<SignInResponse>().data?.token ?: run {
                    authPreference.token().delete()
                    throw NullDataResponseException()
                }
                authPreference.token().set(token)
                token
            }
        }
    }

    override suspend fun signOut() {
        authPreference.token().delete()
    }

}