package dev.achmad.checkin.domain.repository

import android.graphics.Bitmap
import dev.achmad.checkin.domain.model.Attendance
import dev.achmad.checkin.domain.model.User
import java.io.File

interface AttendanceRepository {
    suspend fun getAttendanceHistory(): List<Attendance>
    suspend fun getPendingAttendances(): List<Attendance>
    suspend fun approvePendingAttendance(data: Attendance)
    suspend fun rejectPendingAttendance(data: Attendance)
    suspend fun submitAttendance(
        isManual: Boolean,
        user: User,
        location: Attendance.Location,
        type: Attendance.Type,
        timestamp: Long,
        photo: Bitmap,
        attachments: List<File> = emptyList(),
    ): Attendance
}