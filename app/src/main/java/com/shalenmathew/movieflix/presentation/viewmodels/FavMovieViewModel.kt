package com.shalenmathew.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.IdAndMovieResult
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.domain.usecases.FavMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavMovieViewModel @Inject constructor(private val favMovie: FavMovie, private val dao: com.shalenmathew.movieflix.data.local_storage.MovieDao, @dagger.hilt.android.qualifiers.ApplicationContext private val app: android.content.Context) : ViewModel() {

    fun insertFavMovieData(movieResult: MovieResult) {
        val favMovieData = IdAndMovieResult(movieResult.id!!, movieResult)
        viewModelScope.launch {
            if(dao.getFavMovieById(movieResult.id!!) != null)return@launch
            favMovie.insertFavMovie(favMovieData)
            dao.updateWatchedDate(movieResult.id!!,com.shalenmathew.movieflix.core.utils.DiaryDate.today()+" 12:00:00")
            val path=movieResult.posterPath
            if(path!=null&&path.startsWith("/")&&path.count{it=='/'}==1) {
                runCatching {
                    val file=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO){
                        okhttp3.OkHttpClient().newCall(okhttp3.Request.Builder().url(com.shalenmathew.movieflix.core.utils.Constants.TMDB_POSTER_IMAGE_BASE_URL_W342+path).build()).execute().use{response->
                            check(response.isSuccessful);response.body.byteStream().use{com.shalenmathew.movieflix.core.utils.DiaryImages.copyStream(app,it,"poster_")}
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
        val date = com.shalenmathew.movieflix.core.utils.DiaryDate.watchedMonthTimestamp(year, month)
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
            favMovie.insertGalleryImage(com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity(mediaId = id, imagePath = imagePath))
        }
    }

    fun deleteGalleryImage(image: com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity) {
        viewModelScope.launch {
            favMovie.deleteGalleryImage(image)
        }
    }

    fun getGalleryImages(mediaId: Int): LiveData<List<com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity>> {
        return favMovie.getGalleryImagesForMedia(mediaId)
    }

    fun getAllGalleryImages() = dao.getAllGalleryImages()

}
