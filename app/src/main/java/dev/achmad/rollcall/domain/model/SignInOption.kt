package dev.achmad.rollcall.domain.model

import dev.achmad.rollcall.core.util.enumValueOfOrDefault

interface SignInOption {

    enum class Name {
        BASIC, GOOGLE, UNKNOWN;
        operator fun invoke(value: String?) = enumValueOfOrDefault(value, UNKNOWN)
    }

    data class Basic(
        val username: String,
        val password: String,
    ): SignInOption

    data object Google: SignInOption

}
