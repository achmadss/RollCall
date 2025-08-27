package dev.achmad.checkin.core.location

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.location.LocationManager as AndroidLocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

sealed class LocationState {
    data object Idle : LocationState()
    data object Loading : LocationState()
    data class Success(val location: LocationData) : LocationState()
    data class Error(val message: String, val throwable: Throwable? = null) : LocationState()
    data object PermissionDenied : LocationState()
    data object LocationDisabled : LocationState()
}

class LocationEnabledState internal constructor(
    val isEnabled: MutableState<Boolean>,
    val openLocationSettings: () -> Unit
)

@Composable
fun rememberLocationEnabledState(): LocationEnabledState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
    }

    val locationEnabled = remember {
        mutableStateOf(
            locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    locationEnabled.value = locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
                            locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return remember {
        LocationEnabledState(
            isEnabled = locationEnabled,
            openLocationSettings = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
        )
    }
}