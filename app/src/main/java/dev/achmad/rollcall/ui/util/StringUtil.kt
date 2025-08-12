package dev.achmad.rollcall.ui.util

fun String.splitCamelCase(): String {
    return replace(Regex("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"), " ")
}
