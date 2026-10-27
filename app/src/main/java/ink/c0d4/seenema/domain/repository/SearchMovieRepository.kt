package ink.c0d4.seenema.domain.repository

import ink.c0d4.seenema.core.utils.NetworkResults
import ink.c0d4.seenema.domain.model.MovieList
import kotlinx.coroutines.flow.Flow

interface SearchMovieRepository {
    fun searchMovie(query:String): Flow<NetworkResults<MovieList>>

    fun trendingMovies():Flow<NetworkResults<MovieList>>
}