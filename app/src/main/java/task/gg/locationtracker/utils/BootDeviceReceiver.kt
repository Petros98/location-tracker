package task.gg.locationtracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import task.gg.locationtracker.data.service.LocationService

class BootDeviceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            ContextCompat.startForegroundService(it, Intent(it, LocationService::class.java))
        }
    }
}