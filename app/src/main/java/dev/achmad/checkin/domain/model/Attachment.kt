package dev.achmad.checkin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("uploaded_by") val uploadedBy: User? = null,
    @SerialName("attendance") val attendance: Attendance? = null,
    @SerialName("original_filename") val originalFilename: String? = null,
    @SerialName("filename") val fileName: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("size") val size: Double? = null,
    @SerialName("created_at") val createdAt: String,
)
