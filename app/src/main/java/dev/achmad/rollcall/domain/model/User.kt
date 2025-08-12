package dev.achmad.rollcall.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val companyId: String,
    val role: Role,
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)