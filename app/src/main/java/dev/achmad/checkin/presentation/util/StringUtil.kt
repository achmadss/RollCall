package dev.achmad.checkin.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun String.splitCamelCase(): String {
    return replace(Regex("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"), " ")
}

fun String.toComposeColor(): Color {
    return try {
        Color(this.toColorInt())
    } catch (e: Exception) {
        e.printStackTrace()
        Color.Transparent
    }
}
