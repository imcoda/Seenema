package ink.c0d4.seenema.domain.usecases

import androidx.lifecycle.LiveData
import ink.c0d4.seenema.data.local_storage.entity.IdAndMovieResult
import ink.c0d4.seenema.data.local_storage.entity.WatchListEntity
import ink.c0d4.seenema.domain.repository.WatchListRepository
import javax.inject.Inject

class WatchList @Inject constructor(private val watchListRepository: WatchListRepository) {

    suspend fun insertWatchListData(idAndMovieResult: IdAndMovieResult){
    watchListRepository.insertWatchListData(idAndMovieResult)
}

    suspend fun deleteWatchListData(watchListEntity: WatchListEntity){
        watchListRepository.deleteWatchListData(watchListEntity)
    }

     fun getAllWatchListData():LiveData<List<WatchListEntity>>{
        return  watchListRepository.getAllWatchListData()
    }

    suspend fun updateWatchListPoster(id: Int, posterPath: String) {
        watchListRepository.updateWatchListPoster(id, posterPath)
    }

    suspend fun updateWatchListBanner(id: Int, bannerPath: String) {
        watchListRepository.updateWatchListBanner(id, bannerPath)
    }


}