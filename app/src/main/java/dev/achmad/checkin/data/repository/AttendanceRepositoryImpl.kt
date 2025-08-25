package dev.achmad.checkin.data.repository

import android.graphics.Bitmap
import dev.achmad.checkin.core.network.GET
import dev.achmad.checkin.core.network.MULTIPART_POST
import dev.achmad.checkin.core.network.NetworkHelper
import dev.achmad.checkin.core.network.POST
import dev.achmad.checkin.core.network.awaitBaseResponse
import dev.achmad.checkin.data.remote.model.base.NullDataResponseException
import dev.achmad.checkin.domain.API_URL_V1
import dev.achmad.checkin.domain.model.Attendance
import dev.achmad.checkin.domain.model.User
import dev.achmad.checkin.domain.repository.AttendanceRepository
import java.io.File
import java.util.UUID

class AttendanceRepositoryImpl(
    private val networkHelper: NetworkHelper,
): AttendanceRepository {

    override suspend fun getAttendanceHistory(): List<Attendance> {
        return networkHelper.client.newCall(
            GET("$API_URL_V1/attendances")
        ).awaitBaseResponse<List<Attendance>>().data
            ?: throw NullDataResponseException()
    }

    override suspend fun getPendingAttendances(): List<Attendance> {
        return networkHelper.client.newCall(
            GET("$API_URL_V1/attendances?pending=true")
        ).awaitBaseResponse<List<Attendance>>().data
            ?: throw NullDataResponseException()
    }

    override suspend fun approvePendingAttendance(data: Attendance) {
        networkHelper.client.newCall(
            POST("$API_URL_V1/attendances/approve/${data.id}")
        ).awaitBaseResponse<Unit?>()
    }

    override suspend fun rejectPendingAttendance(data: Attendance) {
        networkHelper.client.newCall(
            POST("$API_URL_V1/attendances/reject/${data.id}")
        ).awaitBaseResponse<Unit?>()
    }

    override suspend fun submitAttendance(
        isManual: Boolean,
        user: User,
        location: Attendance.Location,
        type: Attendance.Type,
        timestamp: Long,
        photo: Bitmap,
        attachments: List<File>
    ): Attendance {
        val status = if (isManual) Attendance.Status.ACCEPTED else Attendance.Status.PENDING
        return networkHelper.client.newCall(
            MULTIPART_POST(
                url = "$API_URL_V1/attendances",
                formData = mapOf(
                    "user_id" to user.id,
                    "status" to status.name,
                    "latitude" to location.latitude.toString(),
                    "user_id" to location.longitude.toString(),
                    "timestamp" to timestamp.toString(),
                ),
                files = attachments.associateBy { UUID.randomUUID().toString() }
            )
        ).awaitBaseResponse<Attendance>().data
            ?: throw NullDataResponseException()
    }

}