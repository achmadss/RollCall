package dev.achmad.checkin.data.remote.model.signin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
sealed interface SignInRequest {

    @Serializable
    data class Basic(
        val username: String,
        val password: String,
    ): SignInRequest

    fun toRequestBody(): RequestBody {
        val jsonString = Json.encodeToString(this)
        return jsonString.toRequestBody("application/json".toMediaType())
    }

}