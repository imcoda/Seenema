package ink.c0d4.seenema.domain.usecases

import ink.c0d4.seenema.domain.repository.ActorRepository
import javax.inject.Inject

class GetActorInfo @Inject constructor(
    private val actorRepository: ActorRepository
) {
    fun getActorDetail(personId: Int) = actorRepository.getActorDetail(personId)
    
    fun getActorMoviesAndShows(personId: Int) = actorRepository.getActorMoviesAndShows(personId)
}
