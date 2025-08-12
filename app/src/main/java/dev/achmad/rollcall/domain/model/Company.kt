package dev.achmad.rollcall.domain.model

data class Company(
    val id: String,
    val name: String,
    val code: String,
    val signInOptions: List<SignInOption.Name>,
    val createdAt: Long,
    val updatedAt: Long,
)
