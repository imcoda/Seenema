package ink.c0d4.seenema.data.local_storage

import androidx.lifecycle.LiveData
import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity
import ink.c0d4.seenema.data.local_storage.entity.HomeFeedEntity
import ink.c0d4.seenema.data.local_storage.entity.IdAndMovieResult
import ink.c0d4.seenema.data.local_storage.entity.WatchListEntity
import ink.c0d4.seenema.data.local_storage.entity.ScheduledEntity
import javax.inject.Inject

class LocalDataSource @Inject constructor(private val movieDao: MovieDao) {
    suspend fun insertHomeFeedData(homeFeedEntity: HomeFeedEntity){
        movieDao.insertHomeFeedData(homeFeedEntity)
    }
    suspend  fun readHomeFeedData():HomeFeedEntity{
        return movieDao.readHomeFeedData()
    }
    suspend fun deleteAllHomeFeedData(){
        movieDao.deleteHomeFeedData()
    }

    suspend fun insertWatchListData(idAndMovieResult: IdAndMovieResult){
        movieDao.insertWatchListData(idAndMovieResult)
    }

    suspend fun deleteWatchListData(watchListEntity: WatchListEntity){
        movieDao.deleteWatchListData(watchListEntity)
    }

    fun getAllWatchListData():LiveData<List<WatchListEntity>>{
        return movieDao.getAllWatchListData()
    }

    suspend fun isMovieInWatchList(id: Int): Boolean = movieDao.isMovieInWatchList(id)

    suspend fun getWatchListMovieById(id: Int): WatchListEntity? = movieDao.getWatchListMovieById(id)

    suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult){
        movieDao.insertFavMovie(idAndMovieResult)
    }

    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity){
        movieDao.deleteFavMovie(favouritesEntity)
    }

    fun getAllFavMovie():LiveData<List<FavouritesEntity>>{
        return movieDao.getAllFavMovies()
    }

    suspend fun getFavMovieById(id: Int): FavouritesEntity? = movieDao.getFavMovieById(id)

    suspend fun isMovieInFavorites(id: Int): Boolean = movieDao.isMovieInFavorites(id)

    suspend fun addPersonalNote(favoriteId: Int, personalNote: String?) {
        return movieDao.addPersonalNote(favoriteId, personalNote)
    }

    suspend fun updateFavMovieResult(id: Int, movieResult: ink.c0d4.seenema.domain.model.MovieResult) {
        movieDao.updateFavMovieResult(id, movieResult)
    }

    suspend fun updateWatchListMovieResult(id: Int, movieResult: ink.c0d4.seenema.domain.model.MovieResult) {
        movieDao.updateWatchListMovieResult(id, movieResult)
    }

    suspend fun insertScheduledMovie(scheduledEntity: ScheduledEntity) {
        movieDao.insertScheduledMovie(scheduledEntity)
    }

    suspend fun deleteScheduledMovie(scheduledEntity: ScheduledEntity) {
        movieDao.deleteScheduledMovie(scheduledEntity)
    }

    fun getAllScheduledMovies(): LiveData<List<ScheduledEntity>> {
        return movieDao.getAllScheduledMovies()
    }

    suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity? {
        return movieDao.getScheduledMovieById(movieId)
    }

    suspend fun insertGalleryImage(image: ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity) {
        movieDao.insertGalleryImage(image)
    }

    suspend fun deleteGalleryImage(image: ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity) {
        movieDao.deleteGalleryImage(image)
    }

    fun getGalleryImagesForMedia(mediaId: Int): LiveData<List<ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity>> {
        return movieDao.getGalleryImagesForMedia(mediaId)
    }
}