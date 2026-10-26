package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.GalleryTimeline
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import java.text.SimpleDateFormat
import java.util.Locale

/** Adds full-width month headings around the unchanged upstream poster cards. */
class GalleryTimelineAdapter(
    onPosterClick: (MovieResult) -> Unit,
    onLongClick: (View, MovieResult) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private data class Row(val movie: FavouritesEntity? = null, val month: String? = null)
    private val posters = FavAdapters(onPosterClick, onLongClick)
    private var rows = emptyList<Row>()

    fun submitList(movies: List<FavouritesEntity>) {
        rows = GalleryTimeline.sections(movies).flatMap { section ->
            listOf(Row(month = section.month)) + section.movies.map { Row(movie = it) }
        }
        notifyDataSetChanged()
    }

    fun isHeading(position: Int) = rows.getOrNull(position)?.movie == null
    fun updateScheduledMovies(ids: Set<Int>) {
        posters.updateScheduledMovies(ids)
        notifyDataSetChanged()
    }

    override fun getItemCount() = rows.size
    override fun getItemViewType(position: Int) = if (isHeading(position)) 1 else 0

    private class MonthHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == 1) MonthHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_month, parent, false))
        else posters.onCreateViewHolder(parent, 0)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = rows[position]
        if (row.movie != null) {
            (holder as FavAdapters.ViewHolder).bind(row.movie)
        } else {
            val label = holder.itemView as TextView
            label.text = row.month?.let { month ->
                val locale = label.resources.configuration.locales[0] ?: Locale.getDefault()
                val date = SimpleDateFormat("yyyy-MM", Locale.ROOT).parse(month)!!
                val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "yMMMM")
                SimpleDateFormat(pattern, locale).format(date)
            } ?: label.context.getString(R.string.gallery_undated)
        }
    }
}
