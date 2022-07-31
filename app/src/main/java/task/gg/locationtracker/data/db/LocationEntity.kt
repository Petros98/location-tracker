package task.gg.locationtracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "location_table")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val routeId: UUID = UUID.randomUUID(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val foreground: Boolean = true
)
