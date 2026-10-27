package ink.c0d4.seenema.data.local_storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity
import ink.c0d4.seenema.data.local_storage.entity.HomeFeedEntity
import ink.c0d4.seenema.data.local_storage.entity.WatchListEntity
import ink.c0d4.seenema.data.local_storage.entity.ScheduledEntity
import ink.c0d4.seenema.data.local_storage.entity.SeriesTrackingEntity
import ink.c0d4.seenema.data.local_storage.entity.SeasonTrackingEntity
import ink.c0d4.seenema.data.local_storage.entity.EpisodeTrackingEntity

@Database(
    entities = [
        HomeFeedEntity::class,
        WatchListEntity::class,
        FavouritesEntity::class,
        ScheduledEntity::class,
        SeriesTrackingEntity::class,
        SeasonTrackingEntity::class,
        EpisodeTrackingEntity::class,
        ink.c0d4.seenema.data.local_storage.entity.SeriesProgressEntity::class,
        ink.c0d4.seenema.data.local_storage.entity.CustomListEntity::class,
        ink.c0d4.seenema.data.local_storage.entity.CustomListMovieEntity::class,
        ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity::class
    ],
    version = 17
)
@TypeConverters(MovieDataTypeConverter::class)
abstract class MovieDatabase : RoomDatabase() {

    abstract val dao: MovieDao
    abstract val seriesTrackingDao: SeriesTrackingDao
    abstract val customListDao: CustomListDao
}
