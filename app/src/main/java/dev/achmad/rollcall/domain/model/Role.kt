package dev.achmad.rollcall.domain.model

import dev.achmad.rollcall.core.util.enumValueOfOrDefault

enum class Role {
    EMPLOYEE, ADMIN, MANAGER, UNKNOWN;
    operator fun invoke(value: String?) = enumValueOfOrDefault(value, UNKNOWN)
}