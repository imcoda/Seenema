package ink.c0d4.seenema.domain.repository

import ink.c0d4.seenema.core.utils.NetworkResults
import ink.c0d4.seenema.domain.model.ActorDetail
import ink.c0d4.seenema.domain.model.MovieResult
import kotlinx.coroutines.flow.Flow

interface ActorRepository {
    fun getActorDetail(personId: Int): Flow<NetworkResults<ActorDetail>>
    fun getActorMoviesAndShows(personId: Int): Flow<NetworkResults<List<MovieResult>>>
}
