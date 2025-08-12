package dev.achmad.rollcall.domain.model

import dev.achmad.rollcall.core.util.enumValueOfOrDefault

data class Amendment(
    val id: String,
    val attendance: Attendance,
    val requestTimestamp: Long,
    val reason: String,
    val status: Status,
    val manager: User,
    val managerComment: String,
    val createdAt: Long,
    val statusUpdatedAt: Long,
) {
    enum class Status {
        PENDING, ACCEPTED, REJECTED;
        operator fun invoke(value: String?) = enumValueOfOrDefault(value, PENDING)
    }
}
