package ink.c0d4.seenema.data.remote

import ink.c0d4.seenema.data.model.ActorDetailResponse
import ink.c0d4.seenema.data.model.ActorImagesResponse
import ink.c0d4.seenema.data.model.ActorMovieCreditsResponse
import ink.c0d4.seenema.data.model.ActorTVCreditsResponse
import ink.c0d4.seenema.data.model.CastResponse
import ink.c0d4.seenema.data.model.MovieResponseList
import ink.c0d4.seenema.data.model.MovieResponseVideoResultList
import ink.c0d4.seenema.data.model.PersonExternalIdsResponse
import ink.c0d4.seenema.data.model.TVDetailResponse
import ink.c0d4.seenema.data.model.TVImagesResponse
import ink.c0d4.seenema.data.model.TVSeasonResponse
import ink.c0d4.seenema.data.model.WhereToWatchProviderResponse
import ink.c0d4.seenema.data.network.ApiClient
import ink.c0d4.seenema.domain.model.MovieResult
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiClient: ApiClient) {

    suspend fun getUpcomingMovies(page: Int = 1):Response<MovieResponseList>{
      return  apiClient.getUpcomingMoviesApiCall(page = page)
    }

    suspend fun getPopularMovies(page: Int = 1):Response<MovieResponseList>{
        return   apiClient.getPopularMoviesApiCall(page = page)
    }

    suspend fun getTrendingMovies(page: Int = 1):Response<MovieResponseList>{
        return apiClient.getTrendingApiCall(page = page)
    }

    suspend fun getTopRatedTV(page: Int = 1):Response<MovieResponseList>{
        return apiClient.getTopRatedTVApiCall(page = page)
    }

    suspend fun getNetflixShows(page: Int = 1):Response<MovieResponseList>{
        return   apiClient.getNetflixShowsApiCall(page = page)
    }

    suspend fun getNowPlayingMovies(page: Int = 1):Response<MovieResponseList>{
        return   apiClient.getNowPlayingMoviesApiCall(page = page)
    }

    suspend fun getAmazonPrimeShows(page: Int = 1):Response<MovieResponseList>{
        return   apiClient.getAmazonPrimeShowsApi(page = page)
    }

    suspend fun getAnime(page: Int = 1): Response<MovieResponseList> {
        return apiClient.getAnimeApiCall(page = page)
    }

    suspend fun getMovieTrailer(movieId:Int):Response<MovieResponseVideoResultList>{
        return apiClient.fetchMovieTrailerApiCall(movieId)
    }

    suspend fun getTVTrailer(tvId:Int):Response<MovieResponseVideoResultList>{
        return apiClient.fetchTVTrailerApiCall(tvId)
    }

    suspend fun getRecommendation(movieId:Int):Response<MovieResponseList>{
        return apiClient.fetchRecommendationApiCall(movieId)
    }

    suspend fun getWhereToWatchProviders(movieId:Int):Response<WhereToWatchProviderResponse>{
        return apiClient.getMovieWatchProvidersApiCall(movieId)
    }

    suspend fun getTVWatchProviders(tvId:Int):Response<WhereToWatchProviderResponse>{
        return apiClient.getTVWatchProvidersApiCall(tvId)
    }

    suspend fun getSearchResult(query:String):Response<MovieResponseList>{
        return apiClient.fetchMovieSearchedResultsApiCall(searchQuery = query)
    }

    suspend fun getMovieCast(movieId: Int): Response<CastResponse> {
        return apiClient.fetchMovieCastApiCall(movieId)
    }

    suspend fun getTVCast(tvId: Int): Response<CastResponse> {
        return apiClient.fetchTVCastApiCall(tvId)
    }

    suspend fun getPersonExternalIds(personId: Int): Response<PersonExternalIdsResponse> {
        return apiClient.fetchPersonExternalIdsApiCall(personId)
    }

    suspend fun getActorDetail(personId: Int): Response<ActorDetailResponse> {
        return apiClient.fetchActorDetailApiCall(personId)
    }

    suspend fun getActorMovieCredits(personId: Int): Response<ActorMovieCreditsResponse> {
        return apiClient.fetchActorMovieCreditsApiCall(personId)
    }

    suspend fun getActorImages(personId: Int): Response<ActorImagesResponse> {
        return apiClient.fetchActorImagesApiCall(personId)
    }

    suspend fun getActorTVCredits(personId: Int): Response<ActorTVCreditsResponse> {
        return apiClient.fetchActorTVCreditsApiCall(personId)
    }

    suspend fun getTVDetail(tvId: Int): Response<TVDetailResponse> {
        return apiClient.fetchTVDetailApiCall(tvId)
    }

    suspend fun getTVImages(tvId: Int, includeImageLanguage: String? = null): Response<TVImagesResponse> {
        return apiClient.fetchTVImagesApiCall(tvId, includeImageLanguage)
    }

    suspend fun getMovieImages(movieId: Int, includeImageLanguage: String? = null): Response<TVImagesResponse> {
        return apiClient.fetchMovieImagesApiCall(movieId, includeImageLanguage)
    }

    suspend fun getTVSeason(tvId: Int, seasonNumber: Int): Response<TVSeasonResponse> {
        return apiClient.fetchTVSeasonApiCall(tvId, seasonNumber)
    }

    suspend fun getMovieDetails(movieId: Int): Response<MovieResult> {
        return apiClient.fetchMovieDetailApiCall(movieId)
    }

}