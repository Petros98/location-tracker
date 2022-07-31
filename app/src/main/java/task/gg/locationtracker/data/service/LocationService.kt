package task.gg.locationtracker.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import task.gg.locationtracker.R
import task.gg.locationtracker.data.LocationRepository
import task.gg.locationtracker.utils.LocationHelper
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationService: Service() {

    @Inject
    lateinit var repository: LocationRepository

    private val NOTIFICATION_CHANNEL_ID = "Tracker Notification"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification()
        } else {
//            startService(Intent(applicationContext, LocationService::class.java))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification() {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(false)
                .setSmallIcon(R.mipmap.ic_launcher)

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = NOTIFICATION_CHANNEL_ID
            notificationManager.createNotificationChannel(notificationChannel)
            startForeground(1, builder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val routeId: UUID = UUID.randomUUID()
        LocationHelper.startListeningUserLocation(
            this
        ) { location ->
            repository.addLocation(location, routeId)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}