package task.gg.locationtracker.data

import android.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import task.gg.locationtracker.data.db.LocationDatabase
import task.gg.locationtracker.data.db.LocationEntity
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    database: LocationDatabase
) {
    private val locationDao = database.locationDao()

    fun addLocation(it: Location, routeId: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            locationDao.addLocation(
                LocationEntity(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    routeId = routeId
                )
            )
        }
    }

    suspend fun getLocations(): List<LocationEntity> {
        return locationDao.getLocations()
    }
}