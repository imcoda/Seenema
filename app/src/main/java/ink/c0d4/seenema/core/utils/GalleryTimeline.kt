package ink.c0d4.seenema.core.utils

import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity

/** The existing timestamp stores the watched date, editable to month precision. */
object GalleryTimeline {
    data class Section(val month: String?, val movies: List<FavouritesEntity>)

    fun monthKey(date: String?): String? = date?.take(10)
        ?.takeIf { DiaryDate.valid(it) }?.take(7)

    fun sections(movies: List<FavouritesEntity>): List<Section> = movies
        .sortedWith(compareByDescending<FavouritesEntity> { monthKey(it.insertedAt).orEmpty() }
            .thenByDescending { it.insertedAt.orEmpty() }.thenByDescending { it.id })
        .groupBy { monthKey(it.insertedAt) }
        .map { (month, films) -> Section(month, films) }
}
