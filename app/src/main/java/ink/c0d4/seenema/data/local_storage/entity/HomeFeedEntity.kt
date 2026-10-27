package ink.c0d4.seenema.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.data.model.HomeFeedResponse
import ink.c0d4.seenema.data.model.MovieResponseResult
import ink.c0d4.seenema.domain.model.HomeFeedData


@Entity(Constants.TABLE_NAME)
data class HomeFeedEntity(
    @PrimaryKey
    val id:Int?=null,
    val bannerMovies:List<MovieResponseResult>,
    val homeFeedResponseList: List<HomeFeedResponse>
)
{
    fun toHomeFeedData(): HomeFeedData {
        return HomeFeedData(bannerMovies.map { it.toMovieResult()}
            ,homeFeedResponseList.map { it.toHomeFeed() })
    }
}