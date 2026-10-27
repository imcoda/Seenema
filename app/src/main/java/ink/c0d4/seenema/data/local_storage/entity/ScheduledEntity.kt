package ink.c0d4.seenema.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.domain.model.MovieResult

@Entity(Constants.SCHEDULED_TABLE_NAME)
data class ScheduledEntity(
    @PrimaryKey val id: Int,
    val movieResult: MovieResult,
    val scheduledDate: Long, // Timestamp in milliseconds
    val customMessage: String? = null
)
