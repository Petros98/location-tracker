package task.gg.locationtracker.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import task.gg.locationtracker.data.LocationRepository
import task.gg.locationtracker.data.db.LocationEntity
import task.gg.locationtracker.data.service.LocationService
import task.gg.locationtracker.utils.hasLocationPermissions
import task.gg.locationtracker.utils.isLocationEnabled
import task.gg.locationtracker.utils.isServiceRunning
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val _uiState by lazy { MutableStateFlow<UiState>(UiState.Default) }
    val uiState: MutableStateFlow<UiState> get() = _uiState

    private val _routes by lazy { MutableStateFlow<Map<UUID, List<LocationEntity>>>(emptyMap()) }
    val routes: StateFlow<Map<UUID, List<LocationEntity>>> get() = _routes

    init {
        getRoutesInfo()
    }

    private fun getRoutesInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _routes.update { locationRepository.getLocations().groupBy { it.routeId } }
        }
    }

    fun onStartClick(context: Context) {
        if (checkRequirements(context)) {
            if (!context.isServiceRunning(LocationService::class.java)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, LocationService::class.java)
                    )
                } else {
                    context.startService(Intent(context, LocationService::class.java))
                }
                _uiState.update { UiState.Started }
            }
        }
    }

    fun onStopClick(context: Context) {
        if (context.isServiceRunning(LocationService::class.java)) {
            context.stopService(Intent(context, LocationService::class.java))
            _uiState.update { UiState.Stopped }
        }
    }

    fun onCheckCurrentState(context: Context) {
        val readyForTracking = checkRequirements(context)
        if (readyForTracking) {
            if (context.isServiceRunning(LocationService::class.java))
                _uiState.update { UiState.Started }
            else
                _uiState.update { UiState.ReadyForTracking }
        }
    }

    fun onResetState() {
        _uiState.update { UiState.Default }
    }

    private fun checkRequirements(context: Context): Boolean {
        var readyForTrackLocation = true
        if (!context.hasLocationPermissions()) {
            _uiState.update { UiState.RequireAction.REQUEST_PERMISSIONS }
            readyForTrackLocation = false
        } else if (!context.isLocationEnabled()) {
            _uiState.update { UiState.RequireAction.ENABLE_LOCATION }
            readyForTrackLocation = false
        }
        return readyForTrackLocation
    }

    fun onCheckTrackingState(context: Context) {
        _uiState.update {
            if (context.isServiceRunning(LocationService::class.java))
                UiState.Started
            else
                UiState.Stopped
        }
    }
}