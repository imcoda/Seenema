package ink.c0d4.seenema.domain.usecases

import ink.c0d4.seenema.core.utils.NetworkResults
import ink.c0d4.seenema.domain.model.CastMember
import ink.c0d4.seenema.domain.model.CrewMember
import ink.c0d4.seenema.domain.model.HomeFeedData
import ink.c0d4.seenema.domain.model.MovieList
import ink.c0d4.seenema.domain.model.MediaVideoResultList
import ink.c0d4.seenema.domain.model.MovieResult
import ink.c0d4.seenema.domain.model.TVDetail
import ink.c0d4.seenema.domain.model.TVSeason
import ink.c0d4.seenema.domain.model.WatchProviders
import ink.c0d4.seenema.domain.repository.MovieInfoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class  GetMovieInfo @Inject constructor(private val movieInfoRepository: MovieInfoRepository) {

    fun getMovieInfo(): Flow<NetworkResults<HomeFeedData>> {
      return  movieInfoRepository.getHomeFeedData()
    }

    fun getMovieTrailer(movieId:Int):Flow<NetworkResults<MediaVideoResultList>>{
        return movieInfoRepository.getMovieTrailer(movieId)
    }

    fun getTVTrailer(tvId:Int):Flow<NetworkResults<MediaVideoResultList>>{
        return movieInfoRepository.getTVTrailer(tvId)
    }

    fun getRecommendation(movieId: Int):Flow<NetworkResults<MovieList>>{
        return  movieInfoRepository.getRecommendation(movieId)
    }

    fun getWhereToWatchProviders(movieId:Int):Flow<NetworkResults<WatchProviders>>{
        return movieInfoRepository.getWhereToWatchProvider(movieId)
    }

    fun getTVWhereToWatchProviders(tvId:Int):Flow<NetworkResults<WatchProviders>>{
        return movieInfoRepository.getTVWhereToWatchProvider(tvId)
    }

    fun loadMoreMoviesForCategory(categoryTitle: String, page: Int): Flow<NetworkResults<MovieList>> {
        return movieInfoRepository.loadMoreMoviesForCategory(categoryTitle, page)
    }

    fun getMovieCast(movieId: Int): Flow<NetworkResults<List<CastMember>>> {
        return movieInfoRepository.getMovieCast(movieId)
    }

    fun getTVCast(tvId: Int): Flow<NetworkResults<List<CastMember>>> {
        return movieInfoRepository.getTVCast(tvId)
    }

    fun getMovieCrew(movieId: Int): Flow<NetworkResults<List<CrewMember>>> {
        return movieInfoRepository.getMovieCrew(movieId)
    }

    fun getTVCrew(tvId: Int): Flow<NetworkResults<List<CrewMember>>> {
        return movieInfoRepository.getTVCrew(tvId)
    }

    fun getTVDetail(tvId: Int): Flow<NetworkResults<TVDetail>> {
        return movieInfoRepository.getTVDetail(tvId)
    }

    fun getTVSeason(tvId: Int, seasonNumber: Int): Flow<NetworkResults<TVSeason>> {
        return movieInfoRepository.getTVSeason(tvId, seasonNumber)
    }

    fun getMovieImages(movieId: Int, includeLanguages: String? = null): Flow<NetworkResults<ink.c0d4.seenema.data.model.TVImagesResponse>> {
        return movieInfoRepository.getMovieImages(movieId, includeLanguages)
    }

    fun getTVImages(tvId: Int, includeLanguages: String? = null): Flow<NetworkResults<ink.c0d4.seenema.data.model.TVImagesResponse>> {
        return movieInfoRepository.getTVImages(tvId, includeLanguages)
    }

    fun getMovieDetails(movieId: Int): Flow<NetworkResults<MovieResult>> {
        return movieInfoRepository.getMovieDetails(movieId)
    }
}