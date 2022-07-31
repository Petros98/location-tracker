package task.gg.locationtracker.data.db

import androidx.room.TypeConverter
import java.util.UUID

class DbTypeConverters {

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }
}
