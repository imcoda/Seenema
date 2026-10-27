package ink.c0d4.seenema.domain.usecases

import androidx.lifecycle.LiveData
import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity
import ink.c0d4.seenema.data.local_storage.entity.IdAndMovieResult
import ink.c0d4.seenema.domain.repository.FavMovieRepository
import javax.inject.Inject

class FavMovie @Inject constructor(private val favMovieRepository: FavMovieRepository) {

    suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult){
        favMovieRepository.insertFavMovie(idAndMovieResult)
    }

    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity){
        favMovieRepository.deleteFavMovie(favouritesEntity)
    }

    fun getAllFavMovie(): LiveData<List<FavouritesEntity>>{
        return favMovieRepository.getAllFavMovie()
    }

    suspend fun updateFavMoviePoster(id: Int, posterPath: String) {
        favMovieRepository.updateFavMoviePoster(id, posterPath)
    }

    suspend fun updateFavMovieBanner(id: Int, bannerPath: String) {
        favMovieRepository.updateFavMovieBanner(id, bannerPath)
    }

    suspend fun addPersonalNote(id: Int, personalNote: String?) {
        return favMovieRepository.addPersonalNote(id, personalNote)
    }

    suspend fun insertGalleryImage(image: ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity) {
        favMovieRepository.insertGalleryImage(image)
    }

    suspend fun deleteGalleryImage(image: ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity) {
        favMovieRepository.deleteGalleryImage(image)
    }

    fun getGalleryImagesForMedia(mediaId: Int): LiveData<List<ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity>> {
        return favMovieRepository.getGalleryImagesForMedia(mediaId)
    }

}