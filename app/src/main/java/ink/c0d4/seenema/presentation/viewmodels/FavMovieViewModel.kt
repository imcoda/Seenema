package ink.c0d4.seenema.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity
import ink.c0d4.seenema.data.local_storage.entity.IdAndMovieResult
import ink.c0d4.seenema.domain.model.MovieResult
import ink.c0d4.seenema.domain.usecases.FavMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavMovieViewModel @Inject constructor(private val favMovie: FavMovie, private val dao: ink.c0d4.seenema.data.local_storage.MovieDao, @dagger.hilt.android.qualifiers.ApplicationContext private val app: android.content.Context) : ViewModel() {

    fun insertFavMovieData(movieResult: MovieResult) {
        val favMovieData = IdAndMovieResult(movieResult.id!!, movieResult)
        viewModelScope.launch {
            if(dao.getFavMovieById(movieResult.id!!) != null)return@launch
            favMovie.insertFavMovie(favMovieData)
            dao.updateWatchedDate(movieResult.id!!,ink.c0d4.seenema.core.utils.DiaryDate.today()+" 12:00:00")
            val path=movieResult.posterPath
            if(path!=null&&path.startsWith("/")&&path.count{it=='/'}==1) {
                runCatching {
                    val file=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO){
                        okhttp3.OkHttpClient().newCall(okhttp3.Request.Builder().url(ink.c0d4.seenema.core.utils.Constants.TMDB_POSTER_IMAGE_BASE_URL_W342+path).build()).execute().use{response->
                            check(response.isSuccessful);response.body.byteStream().use{ink.c0d4.seenema.core.utils.DiaryImages.copyStream(app,it,"poster_")}
                        }
                    }
                    val current=dao.getFavMovieById(movieResult.id!!)
                    if(current?.movieResult?.posterPath==path)dao.updateFavMovieResult(movieResult.id!!,current.movieResult.copy(posterPath=file.path))
                }
            }
        }
    }

    fun getAllMovieData(): LiveData<List<FavouritesEntity>> {
        return favMovie.getAllFavMovie().map { list ->
            list.sortedByDescending { item -> item.insertedAt }
        }
    }

    fun deleteFavMovieData(movieResult: MovieResult) {
        val favMovieEntity = FavouritesEntity(movieResult.id!!, movieResult, insertedAt = null)
        viewModelScope.launch {
            favMovie.deleteFavMovie(favMovieEntity)
        }
    }

    fun addPersonalNote(id: Int, personalNote: String?) {
        viewModelScope.launch {
            favMovie.addPersonalNote(id, personalNote)
        }
    }

    fun updateWatchedMonth(id: Int, year: Int, month: Int) {
        val date = ink.c0d4.seenema.core.utils.DiaryDate.watchedMonthTimestamp(year, month)
        viewModelScope.launch {
            dao.updateWatchedDate(id, date)
        }
    }

    fun updateFavPoster(id: Int, posterPath: String) {
        viewModelScope.launch {
            favMovie.updateFavMoviePoster(id, posterPath)
        }
    }

    fun updateFavBanner(id: Int, bannerPath: String) {
        viewModelScope.launch {
            favMovie.updateFavMovieBanner(id, bannerPath)
        }
    }

    fun insertGalleryImage(id: Int, imagePath: String) {
        viewModelScope.launch {
            favMovie.insertGalleryImage(ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity(mediaId = id, imagePath = imagePath))
        }
    }

    fun deleteGalleryImage(image: ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity) {
        viewModelScope.launch {
            favMovie.deleteGalleryImage(image)
        }
    }

    fun getGalleryImages(mediaId: Int): LiveData<List<ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity>> {
        return favMovie.getGalleryImagesForMedia(mediaId)
    }

    fun getAllGalleryImages() = dao.getAllGalleryImages()

    suspend fun insertFirstGalleryImage(id: Int, imagePath: String) = dao.insertFirstGalleryImage(
        ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity(mediaId = id, imagePath = imagePath))

}
