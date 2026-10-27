package ink.c0d4.seenema.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ink.c0d4.seenema.core.utils.NetworkResults
import ink.c0d4.seenema.domain.model.TrackedEpisode
import ink.c0d4.seenema.domain.model.TrackedSeason
import ink.c0d4.seenema.domain.model.TrackedSeries
import ink.c0d4.seenema.domain.repository.SeriesTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesTrackingViewModel @Inject constructor(
    private val seriesTrackingRepository: SeriesTrackingRepository
) : ViewModel() {

    val allTrackedSeries: LiveData<List<TrackedSeries>> =
        seriesTrackingRepository.getAllTrackedSeries().asLiveData()

    private val _trackingStatus = MutableLiveData<NetworkResults<Unit>>()
    val trackingStatus: LiveData<NetworkResults<Unit>> = _trackingStatus

    private val _isCurrentSeriesTracked = MutableLiveData<Boolean>()
    val isCurrentSeriesTracked: LiveData<Boolean> = _isCurrentSeriesTracked

    private val _currentSeriesProgress = MutableLiveData<TrackedSeries?>()
    val currentSeriesProgress: LiveData<TrackedSeries?> = _currentSeriesProgress

    private val _availableBanners = MutableLiveData<NetworkResults<List<String>>>()
    val availableBanners: LiveData<NetworkResults<List<String>>> = _availableBanners

    fun trackSeries(seriesId: Int) {
        viewModelScope.launch {
            seriesTrackingRepository.trackSeries(seriesId).collectLatest {
                _trackingStatus.value = it
                if (it is NetworkResults.Success) {
                    _isCurrentSeriesTracked.value = true
                }
            }
        }
    }

    fun untrackSeries(seriesId: Int) {
        viewModelScope.launch {
            seriesTrackingRepository.untrackSeries(seriesId)
            _isCurrentSeriesTracked.value = false
        }
    }

    fun checkTrackingStatus(seriesId: Int) {
        // Reset status immediately to avoid showing stale data from the previous show
        _isCurrentSeriesTracked.value = false
        _currentSeriesProgress.value = null
        
        viewModelScope.launch {
            val series = seriesTrackingRepository.getSeriesById(seriesId)
            _isCurrentSeriesTracked.value = seriesTrackingRepository.isSeriesTracked(seriesId)
            _currentSeriesProgress.value = series
        }
    }

    suspend fun isSeriesTrackedDirect(seriesId: Int): Boolean {
        return seriesTrackingRepository.isSeriesTracked(seriesId)
    }

    fun getSeasonsForSeries(seriesId: Int): LiveData<List<TrackedSeason>> {
        return seriesTrackingRepository.getSeasonsForSeries(seriesId).asLiveData()
    }

    fun getEpisodesForSeason(seasonId: Int): LiveData<List<TrackedEpisode>> {
        return seriesTrackingRepository.getEpisodesForSeason(seasonId).asLiveData()
    }

    fun updateEpisodeWatchedStatus(episodeId: Int, isWatched: Boolean) {
        viewModelScope.launch {
            seriesTrackingRepository.updateEpisodeWatchedStatus(episodeId, isWatched)
        }
    }

    fun updateLastWatchedEpisode(seriesId: Int, episodeId: Int, seasonNumber: Int, episodeNumber: Int) {
        viewModelScope.launch {
            seriesTrackingRepository.updateLastWatchedEpisode(seriesId, episodeId, seasonNumber, episodeNumber)
        }
    }

    fun deleteSeriesProgress(seriesId: Int) {
        viewModelScope.launch {
            seriesTrackingRepository.deleteSeriesProgress(seriesId)
        }
    }

    fun markPreviousEpisodesAsWatched(seriesId: Int, seasonNumber: Int, episodeNumber: Int) {
        viewModelScope.launch {
            seriesTrackingRepository.markPreviousEpisodesAsWatched(seriesId, seasonNumber, episodeNumber)
        }
    }

    fun fetchAvailableBanners(seriesId: Int) {
        _availableBanners.value = NetworkResults.Loading()
        viewModelScope.launch {
            _availableBanners.value = seriesTrackingRepository.getTVImages(seriesId)
        }
    }

    fun updateSeriesBanner(seriesId: Int, bannerPath: String) {
        viewModelScope.launch {
            seriesTrackingRepository.updateSeriesBanner(seriesId, bannerPath)
        }
    }

    fun updateSeriesPoster(seriesId: Int, posterPath: String) {
        viewModelScope.launch {
            seriesTrackingRepository.updateSeriesPoster(seriesId, posterPath)
        }
    }
}
