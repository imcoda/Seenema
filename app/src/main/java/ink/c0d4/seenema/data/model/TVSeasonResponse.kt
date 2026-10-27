package ink.c0d4.seenema.data.model

import ink.c0d4.seenema.domain.model.TVSeason

data class TVSeasonResponse(
    val _id: String?,
    val air_date: String?,
    val episodes: List<TVEpisodeResponse>?,
    val name: String?,
    val overview: String?,
    val id: Int?,
    val poster_path: String?,
    val season_number: Int?
) {
    fun toTVSeason(): TVSeason {
        return TVSeason(
            id = id,
            airDate = air_date,
            episodes = episodes?.map { it.toTVEpisode() } ?: emptyList(),
            name = name,
            overview = overview,
            posterPath = poster_path,
            seasonNumber = season_number
        )
    }
}
