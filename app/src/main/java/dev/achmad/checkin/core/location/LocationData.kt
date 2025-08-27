package dev.achmad.checkin.core.location

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val bearing: Float,
    val speed: Float,
    val timestamp: Long,
    val provider: String?
)