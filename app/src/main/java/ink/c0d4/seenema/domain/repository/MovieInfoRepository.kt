package ink.c0d4.seenema.domain.repository

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
import kotlinx.coroutines.flow.Flow

interface MovieInfoRepository {

// creating an abstraction

fun getHomeFeedData():Flow<NetworkResults<HomeFeedData>>
// here a curious question arises why can't we use HomeFeedDataResponse from Data layer instead of HomeDeedData in Flow<NetworkResults<HomeFeedData>>
// i mean both do the same thing right, remember the reason we declared same model class in different layer coz that the each layer dont have to
// be dependent on model class from other layers so every class should use model class from their own layers

fun getMovieTrailer(movieId:Int):Flow<NetworkResults<MediaVideoResultList>>

fun getTVTrailer(tvId:Int):Flow<NetworkResults<MediaVideoResultList>>

fun getRecommendation(movieId:Int):Flow<NetworkResults<MovieList>>

fun getWhereToWatchProvider(movieId:Int):Flow<NetworkResults<WatchProviders>>

fun getTVWhereToWatchProvider(tvId:Int):Flow<NetworkResults<WatchProviders>>

// Pagination support for home feed categories
fun loadMoreMoviesForCategory(categoryTitle: String, page: Int): Flow<NetworkResults<MovieList>>

fun getMovieCast(movieId: Int): Flow<NetworkResults<List<CastMember>>>

fun getTVCast(tvId: Int): Flow<NetworkResults<List<CastMember>>>

fun getMovieCrew(movieId: Int): Flow<NetworkResults<List<CrewMember>>>

fun getTVCrew(tvId: Int): Flow<NetworkResults<List<CrewMember>>>

fun getMovieDetails(movieId: Int): Flow<NetworkResults<MovieResult>>

fun getTVDetail(tvId: Int): Flow<NetworkResults<TVDetail>>

fun getTVSeason(tvId: Int, seasonNumber: Int): Flow<NetworkResults<TVSeason>>

fun getMovieImages(movieId: Int, includeLanguages: String? = null): Flow<NetworkResults<ink.c0d4.seenema.data.model.TVImagesResponse>>

fun getTVImages(tvId: Int, includeLanguages: String? = null): Flow<NetworkResults<ink.c0d4.seenema.data.model.TVImagesResponse>>

}