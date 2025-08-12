package dev.achmad.rollcall.domain.model

data class Attendance(
    val id: String,
    val companyId: String,
    val userId: String,
    val type: Type,
    val timestamp: Long,
    val recordedAt: Long,
    val location: Location,
    val photoUrl: String,
    val attachments: List<Attachment>,
    val amendments: List<Amendment>,
    val createdAt: Long,
    val updatedAt: Long,
) {
    enum class Type { CLOCK_IN, CLOCK_OUT }
}
