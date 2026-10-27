package ink.c0d4.seenema.data.local_storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ink.c0d4.seenema.core.utils.Constants

@Entity(
    tableName = Constants.EPISODES_TRACKING_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = SeriesTrackingEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SeasonTrackingEntity::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["seriesId"]),
        Index(value = ["seasonId"])
    ]
)
data class EpisodeTrackingEntity(
    @PrimaryKey val id: Int,
    val seriesId: Int,
    val seasonId: Int,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String?,
    val overview: String?,
    val stillPath: String?,
    val runtime: Int?,
    val airDate: String?,
    val voteAverage: Double?,
    val isWatched: Boolean = false
) {
    fun toTVEpisode(): ink.c0d4.seenema.domain.model.TVEpisode {
        return ink.c0d4.seenema.domain.model.TVEpisode(
            id = id,
            airDate = airDate,
            episodeNumber = episodeNumber,
            name = name,
            overview = overview,
            runtime = runtime,
            seasonNumber = seasonNumber,
            stillPath = stillPath,
            voteAverage = voteAverage,
            isWatched = isWatched
        )
    }
}
