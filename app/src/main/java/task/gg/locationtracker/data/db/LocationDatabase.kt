package task.gg.locationtracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [LocationEntity::class], version = 1)
@TypeConverters(DbTypeConverters::class)
abstract class LocationDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "location-database"
    }

    abstract fun locationDao(): LocationDao
}
