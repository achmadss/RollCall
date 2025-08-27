package dev.achmad.checkin.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LocationManager(
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Private mutable state flows
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)

    // Public read-only state flows
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    // Channel for one-time location requests
    private val _locationUpdates = Channel<LocationData>(Channel.UNLIMITED)
    val locationUpdates: Flow<LocationData> = _locationUpdates.receiveAsFlow()

    private var currentLocationListener: LocationListener? = null
    private var isRequestingUpdates = false

    /**
     * Request a single location update
     */
    fun requestSingleLocation() {
        scope.launch {
            when {
                !hasLocationPermission() -> {
                    _locationState.value = LocationState.PermissionDenied
                }
                !isLocationServiceEnabled() -> {
                    _locationState.value = LocationState.LocationDisabled
                }
                else -> {
                    _locationState.value = LocationState.Loading
                    requestLocationUpdate(singleUpdate = true)
                }
            }
        }
    }

    /**
     * Start continuous location updates
     */
    fun startLocationUpdates(
        minTimeMs: Long = 5000L,
        minDistanceM: Float = 10f
    ) {
        if (isRequestingUpdates) return

        scope.launch {
            when {
                !hasLocationPermission() -> {
                    _locationState.value = LocationState.PermissionDenied
                }
                !isLocationServiceEnabled() -> {
                    _locationState.value = LocationState.LocationDisabled
                }
                else -> {
                    _locationState.value = LocationState.Loading
                    requestLocationUpdate(
                        singleUpdate = false,
                        minTimeMs = minTimeMs,
                        minDistanceMs = minDistanceM
                    )
                }
            }
        }
    }

    /**
     * Stop location updates
     */
    fun stopLocationUpdates() {
        currentLocationListener?.let { listener ->
            locationManager.removeUpdates(listener)
            currentLocationListener = null
            isRequestingUpdates = false
        }
        _locationState.value = LocationState.Idle
    }

    /**
     * Get last known location
     */
    fun getLastKnownLocation(): Flow<LocationState> = flow {
        emit(LocationState.Loading)

        when {
            !hasLocationPermission() -> {
                emit(LocationState.PermissionDenied)
            }
            !isLocationServiceEnabled() -> {
                emit(LocationState.LocationDisabled)
            }
            else -> {
                try {
                    val providers = listOf(
                        LocationManager.GPS_PROVIDER,
                        LocationManager.NETWORK_PROVIDER,
                        LocationManager.PASSIVE_PROVIDER
                    )

                    var bestLocation: Location? = null
                    var bestTime = Long.MIN_VALUE

                    for (provider in providers) {
                        if (locationManager.isProviderEnabled(provider)) {
                            val location = locationManager.getLastKnownLocation(provider)
                            if (location != null && location.time > bestTime) {
                                bestLocation = location
                                bestTime = location.time
                            }
                        }
                    }

                    if (bestLocation != null) {
                        val locationData = bestLocation.toLocationData()
                        emit(LocationState.Success(locationData))
                    } else {
                        emit(LocationState.Error("No last known location available"))
                    }
                } catch (e: SecurityException) {
                    emit(LocationState.PermissionDenied)
                } catch (e: Exception) {
                    emit(LocationState.Error("Failed to get last known location", e))
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun requestLocationUpdate(
        singleUpdate: Boolean,
        minTimeMs: Long = 0L,
        minDistanceMs: Float = 0f
    ) {
        try {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val locationData = location.toLocationData()
                    _locationState.value = LocationState.Success(locationData)

                    // Emit to the updates channel as well
                    scope.launch {
                        _locationUpdates.send(locationData)
                    }

                    if (singleUpdate) {
                        stopLocationUpdates()
                    }
                }

                override fun onProviderEnabled(provider: String) {
                    // Location enabled state is now handled by Compose state
                }

                override fun onProviderDisabled(provider: String) {
                    if (!isLocationServiceEnabled()) {
                        _locationState.value = LocationState.LocationDisabled
                    }
                }

            }

            currentLocationListener = listener
            isRequestingUpdates = !singleUpdate

            // Try GPS first, fallback to network
            val providers = mutableListOf<String>()
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                providers.add(LocationManager.GPS_PROVIDER)
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                providers.add(LocationManager.NETWORK_PROVIDER)
            }

            if (providers.isEmpty()) {
                _locationState.value = LocationState.LocationDisabled
                return
            }

            // Request updates from the best available provider
            val provider = providers.firstOrNull() ?: LocationManager.NETWORK_PROVIDER
            locationManager.requestLocationUpdates(
                provider,
                minTimeMs,
                minDistanceMs,
                listener,
                Looper.getMainLooper()
            )

        } catch (e: SecurityException) {
            _locationState.value = LocationState.PermissionDenied
        } catch (e: Exception) {
            _locationState.value = LocationState.Error("Failed to request location updates", e)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationServiceEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun Location.toLocationData(): LocationData {
        return LocationData(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            altitude = altitude,
            bearing = bearing,
            speed = speed,
            timestamp = time,
            provider = provider
        )
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopLocationUpdates()
        _locationUpdates.close()
    }
}