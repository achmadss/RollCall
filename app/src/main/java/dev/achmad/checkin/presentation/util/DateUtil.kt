package dev.achmad.checkin.presentation.util

import android.content.Context
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun Long.formatDate(
    pattern: String = "MMMM d, yyyy"
): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    val date = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return formatter.format(date)
}

fun Context.firstInstallTime(): Long {
    return packageManager.getPackageInfo(packageName, 0).firstInstallTime;
}

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

fun getCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

fun getCurrentDate(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy (EEE)", Locale.getDefault())
    return formatter.format(Date())
}