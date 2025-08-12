package dev.achmad.rollcall.domain.model

data class Attachment(
    val id: String,
    val url: String,
    val attendance: Attendance? = null,
    val company: Company? = null,
    val amendment: Amendment,
    val uploadedBy: User,
    val fileName: String? = null,
    val mimeType: String? = null,
    val size: Double? = null,
    val createdAt: String,
)
