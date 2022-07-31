package task.gg.locationtracker.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*

object LocationHelper {

    private var LOCATION_REFRESH_TIME = 5000
    private var LOCATION_REFRESH_DISTANCE = 10

    @SuppressLint("MissingPermission")
    fun startListeningUserLocation(context: Context, onLocationChange: (Location) -> Unit) {
        val mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                onLocationChange(p0)
            }

            override fun onProviderDisabled(provider: String) {
            }

            override fun onProviderEnabled(provider: String) {
            }
        }
        mLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_REFRESH_TIME.toLong(),
            LOCATION_REFRESH_DISTANCE.toFloat(),
            locationListener
        )
    }

    @SuppressLint("MissingPermission")
    fun listenLocationChange(
        activity: Activity,
        fusedLocationProviderClient: FusedLocationProviderClient,
        onLocationChange: (Location) -> Unit
    ) {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(activity) { task ->
                if (task.isSuccessful && task.result != null) {
                    onLocationChange(task.result)
                }
            }
        } catch (e: SecurityException) {
        }
    }

    fun enableLocation(activity: Activity, googleApiClient: GoogleApiClient, code: Int) {
        googleApiClient.connect()

        val locationRequest: LocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 30 * 1000
            fastestInterval = 5 * 1000
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val pendingResult: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())

        pendingResult.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(activity, code)
                } catch (e: IntentSender.SendIntentException) {
                    activity.showToast(e.message)
                }
            }
        }
    }
}