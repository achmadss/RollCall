package dev.achmad.checkin.presentation.screens.checkin

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.checkin.core.camera.CameraDeviceState
import dev.achmad.checkin.core.camera.CameraManager
import dev.achmad.checkin.core.camera.PictureCaptureState
import dev.achmad.checkin.core.di.util.inject
import dev.achmad.checkin.core.location.LocationManager
import dev.achmad.checkin.domain.model.Attendance
import dev.achmad.checkin.domain.repository.AttendanceRepository
import dev.achmad.checkin.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckInScreenState(
    val checkInLoading: Boolean = false,
    val checkInSuccess: Boolean? = null,
    val message: String? = null,
    val attendanceHistory: List<Attendance> = emptyList(),
)

class CheckInScreenModel(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    private val attendanceRepository: AttendanceRepository = inject(),
    private val userRepository: UserRepository = inject(),
): ScreenModel {

    val cameraManager = CameraManager(
        context = context,
        lifecycleOwner = lifecycleOwner,
        coroutineScope = screenModelScope,
    )

    val locationManager = LocationManager(context)

    private val _state = MutableStateFlow(CheckInScreenState())
    val state = _state.asStateFlow()

    fun checkIn() {
        screenModelScope.launch {
            val user = userRepository.getCurrentUser()
//            attendanceRepository.submitAttendance(
//                isManual = false,
//                user = user,
//                location = ,
//                type = TODO(),
//                timestamp = TODO(),
//                photo = TODO(),
//                attachments = TODO()
//            )
        }
    }

}