package dev.achmad.checkin.data.remote.model.signin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInResponse(
    @SerialName("token") val token: String,
)