package dev.achmad.rollcall.domain.model

data class Attachment(
    val id: String,
    val url: String,
    val attendanceId: String,
    val companyId: String,
    val amendmentId: String? = null,
    val uploadedBy: String, // user id
    val fileName: String? = null,
    val mimeType: String? = null,
    val size: Double? = null,
    val createdAt: String,
)
