package dev.achmad.rollcall.domain.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
)