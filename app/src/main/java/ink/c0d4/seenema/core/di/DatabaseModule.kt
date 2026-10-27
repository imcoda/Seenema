package ink.c0d4.seenema.core.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.core.utils.GsonParser
import ink.c0d4.seenema.core.utils.MIGRATION_3_4
import ink.c0d4.seenema.core.utils.MIGRATION_4_5
import ink.c0d4.seenema.core.utils.MIGRATION_6_7
import ink.c0d4.seenema.core.utils.MIGRATION_5_6
import ink.c0d4.seenema.core.utils.MIGRATION_7_8
import ink.c0d4.seenema.core.utils.MIGRATION_8_9
import ink.c0d4.seenema.core.utils.MIGRATION_9_10
import ink.c0d4.seenema.core.utils.MIGRATION_10_11
import ink.c0d4.seenema.core.utils.MIGRATION_11_12
import ink.c0d4.seenema.core.utils.MIGRATION_12_13
import ink.c0d4.seenema.core.utils.MIGRATION_13_14
import ink.c0d4.seenema.core.utils.MIGRATION_14_15
import ink.c0d4.seenema.core.utils.MIGRATION_15_16
import ink.c0d4.seenema.core.utils.MIGRATION_16_17
import ink.c0d4.seenema.data.local_storage.LocalDataSource
import ink.c0d4.seenema.data.local_storage.MovieDao
import ink.c0d4.seenema.data.local_storage.MovieDataTypeConverter
import ink.c0d4.seenema.data.local_storage.MovieDatabase
import ink.c0d4.seenema.data.local_storage.SeriesTrackingDao
import ink.c0d4.seenema.data.local_storage.CustomListDao
import ink.c0d4.seenema.data.remote.RemoteDataSource
import ink.c0d4.seenema.data.repository.ActorRepositoryImpl
import ink.c0d4.seenema.data.repository.FavMovieRepositoryImpl
import ink.c0d4.seenema.data.repository.MovieDetailsRepositoryImpl
import ink.c0d4.seenema.data.repository.ScheduledRepositoryImpl
import ink.c0d4.seenema.data.repository.SeriesTrackingRepositoryImpl
import ink.c0d4.seenema.data.repository.CustomListRepositoryImpl
import ink.c0d4.seenema.data.repository.WatchListRepositoryImpl
import ink.c0d4.seenema.domain.repository.ActorRepository
import ink.c0d4.seenema.domain.repository.FavMovieRepository
import ink.c0d4.seenema.domain.repository.MovieInfoRepository
import ink.c0d4.seenema.domain.repository.ScheduledRepository
import ink.c0d4.seenema.domain.repository.SeriesTrackingRepository
import ink.c0d4.seenema.domain.repository.CustomListRepository
import ink.c0d4.seenema.domain.repository.WatchListRepository
import ink.c0d4.seenema.core.notifications.MovieScheduler
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun providesMovieDataBase(@ApplicationContext context: Context): MovieDatabase {
        return Room.databaseBuilder(context, MovieDatabase::class.java, Constants.DATABASE_NAME)
//            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .addMigrations(
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15,
                MIGRATION_15_16,
                MIGRATION_16_17
            )
            .addTypeConverter(MovieDataTypeConverter(GsonParser(Gson())))
            .build()
    }

    @Provides
    @Singleton
    fun providesMovieDao(movieDatabase: MovieDatabase): MovieDao {
        return movieDatabase.dao
    }

    @Provides
    @Singleton
    fun providesSeriesTrackingDao(movieDatabase: MovieDatabase): SeriesTrackingDao {
        return movieDatabase.seriesTrackingDao
    }

    @Provides
    @Singleton
    fun providesCustomListDao(movieDatabase: MovieDatabase): CustomListDao {
        return movieDatabase.customListDao
    }

    @Provides
    @Singleton
    fun providesMovieInfoRepositoryImpl(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        seriesTrackingDao: SeriesTrackingDao,
        application: Application
    ): MovieInfoRepository {
        return MovieDetailsRepositoryImpl(remoteDataSource, localDataSource, seriesTrackingDao, application)
    }

    @Provides
    @Singleton
    fun getWatchList(
        localDataSource: LocalDataSource,
        seriesTrackingRepository: SeriesTrackingRepository
    ): WatchListRepository {
        return WatchListRepositoryImpl(localDataSource, seriesTrackingRepository)
    }

    @Provides
    @Singleton
    fun getFavMovie(
        localDataSource: LocalDataSource,
        seriesTrackingRepository: SeriesTrackingRepository
    ): FavMovieRepository {
        return FavMovieRepositoryImpl(localDataSource, seriesTrackingRepository)
    }

    @Provides
    @Singleton
    fun providesActorRepository(
        remoteDataSource: RemoteDataSource,
        application: Application
    ): ActorRepository {
        return ActorRepositoryImpl(remoteDataSource, application)
    }

    @Provides
    @Singleton
    fun getScheduled(localDataSource: LocalDataSource): ScheduledRepository {
        return ScheduledRepositoryImpl(localDataSource)
    }

    @Provides
    @Singleton
    fun providesSeriesTrackingRepository(
        seriesTrackingDao: SeriesTrackingDao,
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        @ApplicationContext context: Context
    ): SeriesTrackingRepository {
        return SeriesTrackingRepositoryImpl(seriesTrackingDao, remoteDataSource, localDataSource, context)
    }

    @Provides
    @Singleton
    fun provideMovieScheduler(@ApplicationContext context: Context): MovieScheduler {
        return MovieScheduler(context)
    }

    @Provides
    @Singleton
    fun providesCustomListRepository(
        customListDao: CustomListDao
    ): CustomListRepository {
        return CustomListRepositoryImpl(customListDao)
    }

}