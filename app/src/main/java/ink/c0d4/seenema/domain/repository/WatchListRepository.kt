package ink.c0d4.seenema.domain.repository

import androidx.lifecycle.LiveData
import ink.c0d4.seenema.data.local_storage.entity.IdAndMovieResult
import ink.c0d4.seenema.data.local_storage.entity.WatchListEntity

interface WatchListRepository {

  suspend  fun insertWatchListData(idAndMovieResult: IdAndMovieResult)

  suspend  fun deleteWatchListData(watchListEntity: WatchListEntity)

  suspend fun updateWatchListPoster(id: Int, posterPath: String)

  suspend fun updateWatchListBanner(id: Int, bannerPath: String)

   fun getAllWatchListData():LiveData<List<WatchListEntity>>

}