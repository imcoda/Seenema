package ink.c0d4.seenema.data.repository

import androidx.lifecycle.LiveData
import ink.c0d4.seenema.data.local_storage.LocalDataSource
import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity
import ink.c0d4.seenema.data.local_storage.entity.IdAndMovieResult
import ink.c0d4.seenema.domain.repository.FavMovieRepository

class FavMovieRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val seriesTrackingRepository: ink.c0d4.seenema.domain.repository.SeriesTrackingRepository
) : FavMovieRepository {
    override suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult) {
        localDataSource.insertFavMovie(idAndMovieResult)
    }

    override suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity) {
        localDataSource.deleteFavMovie(favouritesEntity)
        seriesTrackingRepository.cleanupOrphanedProgress(favouritesEntity.id)
    }

    override suspend fun updateFavMoviePoster(id: Int, posterPath: String) {
        val fav = localDataSource.getFavMovieById(id)
        fav?.let {
            val updatedMovieResult = it.movieResult.copy(posterPath = posterPath)
            localDataSource.updateFavMovieResult(id, updatedMovieResult)
        }
    }

    override suspend fun updateFavMovieBanner(id: Int, bannerPath: String) {
        val fav = localDataSource.getFavMovieById(id)
        fav?.let {
            val updatedMovieResult = it.movieResult.copy(backdropPath = bannerPath)
            localDataSource.updateFavMovieResult(id, updatedMovieResult)
        }
    }

    override fun getAllFavMovie(): LiveData<List<FavouritesEntity>> {
       return localDataSource.getAllFavMovie()
    }

    override suspend fun addPersonalNote(id: Int, personalNote: String?) {
        return localDataSource.addPersonalNote(id, personalNote)
    }

    override suspend fun insertGalleryImage(image: ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity) {
        localDataSource.insertGalleryImage(image)
    }

    override suspend fun deleteGalleryImage(image: ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity) {
        localDataSource.deleteGalleryImage(image)
    }

    override fun getGalleryImagesForMedia(mediaId: Int): LiveData<List<ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity>> {
        return localDataSource.getGalleryImagesForMedia(mediaId)
    }
}