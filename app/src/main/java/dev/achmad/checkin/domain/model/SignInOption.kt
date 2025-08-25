package dev.achmad.checkin.domain.model

import dev.achmad.checkin.core.util.enumValueOfOrDefault
import kotlinx.serialization.Serializable

sealed interface SignInOption {

    val name: Name

    @Serializable
    enum class Name {
        BASIC, UNKNOWN;
        companion object {
            operator fun invoke(value: String?) = enumValueOfOrDefault(value, UNKNOWN)
        }
    }

    data class Basic(
        val username: String,
        val password: String,
        override val name: Name = Name.BASIC,
    ): SignInOption

}
