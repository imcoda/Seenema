package ink.c0d4.seenema.diary

import ink.c0d4.seenema.core.utils.GalleryTimeline
import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity
import ink.c0d4.seenema.domain.model.MovieResult
import org.junit.Assert.*
import org.junit.Test

class GalleryTimelineTest {
    private fun film(id: Int, date: String?) = FavouritesEntity(id,
        MovieResult(id=id,title="Movie $id",mediaType="movie"),insertedAt=date)

    @Test fun albumGroupsMonthsAndKeepsNewestMoviesFirst() {
        val groups=GalleryTimeline.sections(listOf(film(1,"2025-12-31 23:00:00"),
            film(2,"2026-10-01 12:00:00"),film(3,"2026-10-26 12:00:00")))
        assertEquals(listOf("2026-10","2025-12"),groups.map{it.month})
        assertEquals(listOf(3,2),groups[0].movies.map{it.id})
    }

    @Test fun invalidAndLegacyUndatedMoviesStayVisibleAtTheEnd() {
        val groups=GalleryTimeline.sections(listOf(film(1,null),film(2,"2026-02-30"),
            film(3,"bad-date"),film(4,"2024-02-29 00:00:00")))
        assertEquals(listOf("2024-02",null),groups.map{it.month})
        assertEquals(setOf(1,2,3),groups.last().movies.map{it.id}.toSet())
    }

    @Test fun removingMoviesDoesNotLeaveEmptyMonthHeadings() {
        assertTrue(GalleryTimeline.sections(emptyList()).isEmpty())
        assertEquals(1,GalleryTimeline.sections(listOf(film(7,"2026-10-26"))).size)
    }
}
