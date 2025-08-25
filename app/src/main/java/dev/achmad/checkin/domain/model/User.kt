package dev.achmad.checkin.domain.model

import dev.achmad.checkin.core.util.enumValueOfOrDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("company") val company: Company,
    @SerialName("role")  val role: Role,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("active") val active: Boolean = true,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
) {
    @Serializable
    enum class Role {
        EMPLOYEE, ADMIN, MANAGER, UNKNOWN;
        companion object {
            operator fun invoke(value: String?) = enumValueOfOrDefault(value, UNKNOWN)
        }
    }
}