package dev.achmad.checkin.domain.model

import dev.achmad.checkin.core.util.enumValueOfOrDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attendance(
    @SerialName("id") val id: String,
    @SerialName("user") val user: User,
    @SerialName("company") val company: Company,
    @SerialName("type") val type: Type,
    @SerialName("status") val status: Status,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("recorded_at") val recordedAt: Long,
    @SerialName("location") val location: Location,
    @SerialName("photo_url") val photoUrl: String,
    @SerialName("attachments") val attachments: List<Attachment>,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
) {
    @Serializable
    enum class Type { CLOCK_IN, CLOCK_OUT }

    @Serializable
    enum class Status {
        PENDING, ACCEPTED, REJECTED;
        companion object {
            operator fun invoke(value: String?) = enumValueOfOrDefault(value, PENDING)
        }
    }

    @Serializable
    data class Location(
        @SerialName("latitude") val latitude: Double,
        @SerialName("longitude") val longitude: Double,
        @SerialName("accuracy") val accuracy: Double,
    )
}
