package ink.c0d4.seenema.domain.repository

import ink.c0d4.seenema.domain.model.CustomListMovie
import ink.c0d4.seenema.domain.model.UserCustomList
import kotlinx.coroutines.flow.Flow

interface CustomListRepository {
    fun getAllLists(): Flow<List<UserCustomList>>
    fun getPinnedFavLists(): Flow<List<UserCustomList>>
    fun getPinnedWatchlistLists(): Flow<List<UserCustomList>>
    suspend fun createList(name: String, description: String?): Long
    suspend fun deleteList(listId: Int)
    suspend fun updateListDetails(listId: Int, name: String, description: String?)
    suspend fun updatePinnedToFavStatus(listId: Int, isPinned: Boolean)
    suspend fun updatePinnedToWatchlistStatus(listId: Int, isPinned: Boolean)
    suspend fun addMovieToList(listId: Int, movie: ink.c0d4.seenema.domain.model.MovieResult)
    suspend fun removeMovieFromList(listId: Int, mediaId: Int)
    suspend fun updateMoviePosterAcrossLists(mediaId: Int, posterPath: String)
    suspend fun updateMovieBannerAcrossLists(mediaId: Int, bannerPath: String)
    fun getMoviesInList(listId: Int): Flow<List<CustomListMovie>>
    suspend fun isMovieInList(listId: Int, mediaId: Int): Boolean
}
