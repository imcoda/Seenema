package ink.c0d4.seenema.data.local_storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.domain.model.MovieResult

@Entity(Constants.WATCHLIST_TABLE_NAME)
data class WatchListEntity(
    @PrimaryKey val id:Int,
    val movieResult: MovieResult,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val insertedAt: String?,
)