package ink.c0d4.seenema.data.repository

import androidx.lifecycle.LiveData
import ink.c0d4.seenema.data.local_storage.LocalDataSource
import ink.c0d4.seenema.data.local_storage.entity.ScheduledEntity
import ink.c0d4.seenema.domain.repository.ScheduledRepository

class ScheduledRepositoryImpl(private val localDataSource: LocalDataSource) : ScheduledRepository {
    override suspend fun insertScheduledMovie(scheduledEntity: ScheduledEntity) {
        localDataSource.insertScheduledMovie(scheduledEntity)
    }

    override suspend fun deleteScheduledMovie(scheduledEntity: ScheduledEntity) {
        localDataSource.deleteScheduledMovie(scheduledEntity)
    }

    override fun getAllScheduledMovies(): LiveData<List<ScheduledEntity>> {
        return localDataSource.getAllScheduledMovies()
    }

    override suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity? {
        return localDataSource.getScheduledMovieById(movieId)
    }
}
