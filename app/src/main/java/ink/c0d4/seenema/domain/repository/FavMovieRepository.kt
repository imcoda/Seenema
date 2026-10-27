package ink.c0d4.seenema.domain.repository

import androidx.lifecycle.LiveData
import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity
import ink.c0d4.seenema.data.local_storage.entity.IdAndMovieResult

interface FavMovieRepository {
    suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult)

    suspend fun addPersonalNote(id: Int, personalNote: String?)

    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity)

    suspend fun updateFavMoviePoster(id: Int, posterPath: String)

    suspend fun updateFavMovieBanner(id: Int, bannerPath: String)

    suspend fun insertGalleryImage(image: ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity)

    suspend fun deleteGalleryImage(image: ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity)

    fun getGalleryImagesForMedia(mediaId: Int): LiveData<List<ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity>>

    fun getAllFavMovie(): LiveData<List<FavouritesEntity>>
}