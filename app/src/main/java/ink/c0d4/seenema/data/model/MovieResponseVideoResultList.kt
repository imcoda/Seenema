package ink.c0d4.seenema.data.model

import ink.c0d4.seenema.domain.model.MediaVideoResultList

data class MovieResponseVideoResultList(
    val id: Int?,
    val results: List<MovieResponseVideoResult>?
){
    fun toMovieVideoResultList():MediaVideoResultList{
        return MediaVideoResultList(
            id = id,
            results = results?.map {
            it.toMovieVideoResult()
        })
    }
}