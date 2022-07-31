package task.gg.locationtracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {

    @Query("SELECT * FROM location_table ORDER BY id DESC")
    suspend fun getLocations(): List<LocationEntity>

    @Insert
    suspend fun addLocation(locationEntity: LocationEntity)
}
