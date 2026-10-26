package com.shalenmathew.movieflix.core.utils

import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity

/** Dates are the existing local gallery insertion dates; no database migration. */
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
