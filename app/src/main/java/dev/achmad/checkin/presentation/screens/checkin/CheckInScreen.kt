package dev.achmad.checkin.presentation.screens.checkin

import android.Manifest
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.checkin.core.camera.CameraDeviceState
import dev.achmad.checkin.core.camera.PictureCaptureState
import dev.achmad.checkin.core.location.LocationEnabledState
import dev.achmad.checkin.core.location.LocationState
import dev.achmad.checkin.core.location.rememberLocationEnabledState
import dev.achmad.checkin.domain.model.Attendance
import dev.achmad.checkin.presentation.util.MultiplePermissionsState
import dev.achmad.checkin.presentation.util.PermissionState
import dev.achmad.checkin.presentation.util.formatDate
import dev.achmad.checkin.presentation.util.getCurrentDate
import dev.achmad.checkin.presentation.util.getCurrentTime
import dev.achmad.checkin.presentation.util.rememberMultiplePermissionsState
import dev.achmad.checkin.presentation.util.rememberPermissionState

object CheckInScreen : Screen {
    private fun readResolve(): Any = CheckInScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val screenModel = rememberScreenModel { CheckInScreenModel(context, lifecycleOwner) }

        val deviceState by screenModel.cameraManager.deviceState.collectAsState()
        val pictureCaptureState by screenModel.cameraManager.pictureState.collectAsState()

        val locationState by screenModel.locationManager.locationState.collectAsState()
        val locationEnabledState = rememberLocationEnabledState()

        val state by screenModel.state.collectAsState()
        val shouldCheckIn by remember {
            derivedStateOf {
                when {
                    deviceState !is CameraDeviceState.Ready -> false
                    pictureCaptureState !is PictureCaptureState.Success -> false
                    else -> true
                }
            }
        }

        val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
        val locationPermissionState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )

        LaunchedEffect(cameraPermissionState.isGranted.value) {
            if (cameraPermissionState.isGranted.value) {
                screenModel.cameraManager.initializeCamera()
            }
        }

        LaunchedEffect(shouldCheckIn) {
            if (shouldCheckIn) {
                screenModel.checkIn()
            }
        }

        DisposableEffect(
            locationEnabledState.isEnabled.value,
            locationPermissionState.isAllPermissionsGranted()
        ) {
            if (
                locationEnabledState.isEnabled.value &&
                locationPermissionState.isAllPermissionsGranted()
            ) {
                screenModel.locationManager.startLocationUpdates()
            }
            onDispose { screenModel.locationManager.cleanup() }
        }

        CheckInScreen(
            cameraDeviceState = deviceState,
            pictureCaptureState = pictureCaptureState,
            locationState = locationState,
            locationEnabledState = locationEnabledState,
            state = state,
            cameraPermissionState = cameraPermissionState,
            locationPermissionState = locationPermissionState,
        )

    }

}

@Composable
private fun CheckInScreen(
    cameraDeviceState: CameraDeviceState,
    pictureCaptureState: PictureCaptureState,
    locationState: LocationState,
    locationEnabledState: LocationEnabledState,
    state: CheckInScreenState,
    cameraPermissionState: PermissionState,
    locationPermissionState: MultiplePermissionsState,
) {
    // Show permission screen if permissions are not granted
    if (!cameraPermissionState.isGranted.value || !locationPermissionState.isAllPermissionsGranted()) {
        PermissionRequiredScreen(
            cameraPermissionState = cameraPermissionState,
            locationPermissionState = locationPermissionState
        )
        return
    }

    // Show service setup screen if camera or location services need setup
    if (cameraDeviceState !is CameraDeviceState.Ready ||
        locationState is LocationState.LocationDisabled ||
        !locationEnabledState.isEnabled.value) {

        ServiceSetupScreen(
            cameraDeviceState = cameraDeviceState,
            locationState = locationState,
            locationEnabledState = locationEnabledState,
            onInitializeCamera = {
                // This should be handled by the ScreenModel when permissions are granted
                // But you can add additional initialization logic here if needed
            }
        )
        return
    }

    // Main check-in screen content
    MainCheckInContent(
        cameraDeviceState = cameraDeviceState,
        pictureCaptureState = pictureCaptureState,
        locationState = locationState,
        state = state
    )
}

@Composable
private fun PermissionRequiredScreen(
    cameraPermissionState: PermissionState,
    locationPermissionState: MultiplePermissionsState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "To continue with check-in, we need access to your camera and location",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Camera Permission
                PermissionItem(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    description = "Required to take your check-in photo",
                    isGranted = cameraPermissionState.isGranted.value,
                    onRequestPermission = cameraPermissionState.requestPermission
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Location Permission
                PermissionItem(
                    icon = Icons.Default.LocationOn,
                    title = "Location",
                    description = "Required to verify your check-in location",
                    isGranted = locationPermissionState.isAllPermissionsGranted(),
                    onRequestPermission = locationPermissionState.requestPermissions
                )
            }
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint =
                if (isGranted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Permission granted",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Grant")
            }
        }
    }
}

@Composable
private fun ServiceSetupScreen(
    cameraDeviceState: CameraDeviceState,
    locationState: LocationState,
    locationEnabledState: LocationEnabledState,
    onInitializeCamera: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Setup Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Please enable the required services to continue",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Camera Setup
                ServiceItem(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    state = when (cameraDeviceState) {
                        is CameraDeviceState.Initializing -> "Initializing..."
                        is CameraDeviceState.LoadingPreview -> "Loading preview..."
                        is CameraDeviceState.Ready -> "Ready"
                        is CameraDeviceState.NoCamera -> "No camera available"
                        is CameraDeviceState.Error -> "Error: ${cameraDeviceState.exception.message}"
                        else -> "Not initialized"
                    },
                    isReady = cameraDeviceState is CameraDeviceState.Ready,
                    isLoading = cameraDeviceState is CameraDeviceState.Initializing ||
                            cameraDeviceState is CameraDeviceState.LoadingPreview,
                    hasError = cameraDeviceState is CameraDeviceState.Error ||
                            cameraDeviceState is CameraDeviceState.NoCamera,
                    onAction = onInitializeCamera,
                    actionText = "Initialize"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Location Setup
                ServiceItem(
                    icon = Icons.Default.LocationOn,
                    title = "Location Services",
                    state = when {
                        !locationEnabledState.isEnabled.value -> "Location services disabled"
                        locationState is LocationState.Loading -> "Getting location..."
                        locationState is LocationState.Success -> "Location acquired"
                        locationState is LocationState.Error -> "Error: ${locationState.message}"
                        else -> "Ready"
                    },
                    isReady = locationEnabledState.isEnabled.value && locationState is LocationState.Success,
                    isLoading = locationState is LocationState.Loading,
                    hasError = !locationEnabledState.isEnabled.value || locationState is LocationState.Error,
                    onAction = locationEnabledState.openLocationSettings,
                    actionText = if (!locationEnabledState.isEnabled.value) "Enable" else "Retry"
                )
            }
        }
    }
}

@Composable
private fun ServiceItem(
    icon: ImageVector,
    title: String,
    state: String,
    isReady: Boolean,
    isLoading: Boolean,
    hasError: Boolean,
    onAction: () -> Unit,
    actionText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 16.dp),
            tint = when {
                isReady -> MaterialTheme.colorScheme.primary
                hasError -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = state,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    hasError -> MaterialTheme.colorScheme.error
                    isReady -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
            isReady -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Service ready",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            else -> {
                Button(
                    onClick = onAction,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
private fun MainCheckInContent(
    cameraDeviceState: CameraDeviceState,
    pictureCaptureState: PictureCaptureState,
    locationState: LocationState,
    state: CheckInScreenState
) {
    var showCamera by remember { mutableStateOf(false) }

    if (showCamera && cameraDeviceState is CameraDeviceState.Ready) {
        CameraScreen(
            preview = cameraDeviceState.preview,
            pictureCaptureState = pictureCaptureState,
            onBackPressed = { showCamera = false },
            onTakePicture = {
                // This should trigger the camera manager to take a picture
                // You'll need to pass this callback from the parent composable
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Map Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Placeholder for map - you can integrate actual map here
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Office Location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Back button
                IconButton(
                    onClick = { /* Handle back navigation */ },
                    modifier = Modifier
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }

        // Office Info Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Time and Office Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getCurrentTime(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                            Text(
                                text = "MAIN OFFICE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = getCurrentDate(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Office Address
                    Text(
                        text = "Main Office Building, Business District",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Jakarta, 12345, Indonesia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Coordinates
                    if (locationState is LocationState.Success) {
                        Text(
                            text = "Latitude: ${locationState.location.latitude.format(6)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Longitude: ${locationState.location.longitude.format(6)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Check-in Button
        item {
            Button(
                onClick = { showCamera = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                enabled = pictureCaptureState !is PictureCaptureState.InProgress,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (pictureCaptureState is PictureCaptureState.InProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Taking Photo...")
                } else {
                    Text(
                        text = "Check In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Network Info
        item {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "SSID: Office-WiFi-5G",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "MAC Address: AA:BB:CC:DD:EE:FF",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "IP Address: 192.168.1.100",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // History Section
        item {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // History Items - Updated to use actual Attendance data
        items(state.attendanceHistory) { attendance ->
            AttendanceHistoryItem(attendance = attendance)
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CameraScreen(
    preview: Preview,
    pictureCaptureState: PictureCaptureState,
    onBackPressed: () -> Unit,
    onTakePicture: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { context ->
                PreviewView(context).also { previewView ->
                    preview.surfaceProvider = previewView.surfaceProvider
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Take Selfie for Check-in",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Capture button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (pictureCaptureState is PictureCaptureState.InProgress)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                        .clickable(
                            enabled = pictureCaptureState !is PictureCaptureState.InProgress
                        ) { onTakePicture() },
                    contentAlignment = Alignment.Center
                ) {
                    if (pictureCaptureState is PictureCaptureState.InProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take Picture",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (pictureCaptureState) {
                        is PictureCaptureState.InProgress -> "Taking photo..."
                        is PictureCaptureState.Error -> "Failed to take photo. Tap to retry."
                        else -> "Tap to capture"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Success overlay
        if (pictureCaptureState is PictureCaptureState.Success) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Photo Captured!",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Your check-in photo has been captured successfully. Processing your attendance...",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceHistoryItem(
    attendance: Attendance
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator with attendance status color
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        when (attendance.status) {
                            Attendance.Status.ACCEPTED -> when (attendance.type) {
                                Attendance.Type.CHECK_IN -> MaterialTheme.colorScheme.primary
                                Attendance.Type.CHECK_OUT -> MaterialTheme.colorScheme.error
                            }
                            Attendance.Status.PENDING -> MaterialTheme.colorScheme.tertiary
                            Attendance.Status.REJECTED -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        },
                        CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attendance.timestamp.formatDate("HH:mm"),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${attendance.company.name} • ${attendance.timestamp.formatDate("dd/MM/yyyy (EEE)")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Status badge for pending/rejected
                    if (attendance.status != Attendance.Status.ACCEPTED) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = attendance.status.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (attendance.status) {
                                Attendance.Status.PENDING -> MaterialTheme.colorScheme.tertiary
                                Attendance.Status.REJECTED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier
                                .background(
                                    when (attendance.status) {
                                        Attendance.Status.PENDING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                        Attendance.Status.REJECTED -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        else -> Color.Transparent
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Type indicator
            Text(
                text = when (attendance.type) {
                    Attendance.Type.CHECK_IN -> "IN"
                    Attendance.Type.CHECK_OUT -> "OUT"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = when (attendance.type) {
                    Attendance.Type.CHECK_IN -> MaterialTheme.colorScheme.primary
                    Attendance.Type.CHECK_OUT -> MaterialTheme.colorScheme.error
                },
                modifier = Modifier
                    .background(
                        when (attendance.type) {
                            Attendance.Type.CHECK_IN -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            Attendance.Type.CHECK_OUT -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun Double.format(decimals: Int): String {
    return String.format("%.${decimals}f", this)
}