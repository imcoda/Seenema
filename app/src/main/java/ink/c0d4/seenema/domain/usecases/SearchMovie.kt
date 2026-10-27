package ink.c0d4.seenema.domain.usecases

import ink.c0d4.seenema.core.utils.NetworkResults
import ink.c0d4.seenema.domain.model.MovieList
import ink.c0d4.seenema.domain.repository.SearchMovieRepository
import kotlinx.coroutines.flow.Flow

class SearchMovie (private val searchMovieRepository: SearchMovieRepository) {
    fun searchMovie(query:String):Flow<NetworkResults<MovieList>>{
        return  searchMovieRepository.searchMovie(query)
    }

    fun trendingMovies():Flow<NetworkResults<MovieList>>{
        return searchMovieRepository.trendingMovies()
    }

}