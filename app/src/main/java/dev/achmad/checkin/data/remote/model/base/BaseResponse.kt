package dev.achmad.checkin.data.remote.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    @SerialName("code") val code: Int,
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: T?
)

class NullDataResponseException(
    override val message: String? = "data is null"
): Exception()

class ClientException(
    override val message: String?
): Exception()

data class ServerException(
    override val message: String?
): Exception()