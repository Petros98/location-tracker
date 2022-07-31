package task.gg.locationtracker.utils

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.core.content.ContextCompat

@Suppress("DEPRECATION")
fun <T> Context.isServiceRunning(service: Class<T>) =
    (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == service.name }

fun Context.hasLocationPermissions() : Boolean {
    val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
    val result1 = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
    return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
}

fun Context.isLocationEnabled(): Boolean {
    return (getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun Context.showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    message?.let {
        Toast.makeText(this, it, duration).show()
    }
}