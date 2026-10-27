package ink.c0d4.seenema.domain.model

data class TVSeason(
    val id: Int?,
    val airDate: String?,
    val episodes: List<TVEpisode>,
    val name: String?,
    val overview: String?,
    val posterPath: String?,
    val seasonNumber: Int?
)
