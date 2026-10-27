package ink.c0d4.seenema.data.local_storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.domain.model.MovieResult

@Entity(Constants.FAVOURITES_TABLE_NAME)
data class FavouritesEntity(
    @PrimaryKey val id:Int,
    val movieResult: MovieResult,
    val personalNote: String? = null,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val insertedAt: String?,
)