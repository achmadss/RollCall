package dev.achmad.checkin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Company(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("code") val code: String,
    @SerialName("icon_url") val iconUrl: String,
    @SerialName("sign_in_options") val signInOptions: List<SignInOption.Name>,
    @SerialName("active") val active: Boolean,
    @SerialName("created_at")  val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)
