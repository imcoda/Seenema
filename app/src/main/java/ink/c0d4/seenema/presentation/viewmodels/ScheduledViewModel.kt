package ink.c0d4.seenema.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ink.c0d4.seenema.core.notifications.MovieScheduler
import ink.c0d4.seenema.data.local_storage.entity.ScheduledEntity
import ink.c0d4.seenema.domain.model.MovieResult
import ink.c0d4.seenema.domain.usecases.ScheduledMovies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduledViewModel @Inject constructor(
    private val scheduledMovies: ScheduledMovies,
    private val movieScheduler: MovieScheduler
) : ViewModel() {

    fun insertScheduledMovie(movieResult: MovieResult, scheduledDate: Long, customMessage: String? = null) {
        val scheduledEntity = ScheduledEntity(movieResult.id!!, movieResult, scheduledDate, customMessage)
        viewModelScope.launch {
            scheduledMovies.insertScheduledMovie(scheduledEntity)
            // Schedule the notification
            movieScheduler.scheduleMovieNotification(movieResult, scheduledDate, customMessage)
        }
    }

    fun getAllScheduledMovies(): LiveData<List<ScheduledEntity>> {
        return scheduledMovies.getAllScheduledMovies()
    }

    fun deleteScheduledMovie(movieResult: MovieResult, scheduledDate: Long) {
        val scheduledEntity = ScheduledEntity(movieResult.id!!, movieResult, scheduledDate)
        viewModelScope.launch {
            scheduledMovies.deleteScheduledMovie(scheduledEntity)
            // Cancel the scheduled notification
            movieResult.id?.let { movieScheduler.cancelMovieNotification(it) }
        }
    }

    suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity? {
        return scheduledMovies.getScheduledMovieById(movieId)
    }

}
