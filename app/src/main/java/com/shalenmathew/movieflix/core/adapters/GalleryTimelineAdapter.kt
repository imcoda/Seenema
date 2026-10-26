package com.shalenmathew.movieflix.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.GalleryTimeline
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import java.text.SimpleDateFormat
import java.util.Locale

class GalleryTimelineAdapter(
    private val onPosterClick: (MovieResult) -> Unit,
    private val onLongClick: (View, MovieResult) -> Unit,
    private val onStillClick: (View, PersonalGalleryEntity) -> Unit = { _, _ -> },
    private val onAddStill: (Int) -> Unit = {}
) : ListAdapter<GalleryTimelineAdapter.Row, RecyclerView.ViewHolder>(RowsDiff) {
    data class Row(val movie: FavouritesEntity? = null, val month: String? = null,
                   val images: List<PersonalGalleryEntity> = emptyList())
    private var movies = emptyList<FavouritesEntity>()
    private var images = emptyMap<Int, List<PersonalGalleryEntity>>()

    fun submitMovies(value: List<FavouritesEntity>) { movies = value; render() }
    fun submitImages(value: List<PersonalGalleryEntity>) {
        images = value.sortedWith(compareByDescending<PersonalGalleryEntity> { it.addedAt }
            .thenByDescending { it.id }).groupBy { it.mediaId }
        render()
    }
    private fun render() {
        submitList(GalleryTimeline.sections(movies).flatMap { section ->
            listOf(Row(month = section.month)) + section.movies.map {
                Row(movie = it, images = images[it.id].orEmpty())
            }
        })
    }
    fun isHeading(position: Int) = currentList.getOrNull(position)?.movie == null
    override fun getItemViewType(position: Int) = if (isHeading(position)) 1 else 0
    private class MonthHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class FilmHolder(view: View) : RecyclerView.ViewHolder(view) {
        val poster: ImageView = view.findViewById(R.id.gallery_film_poster)
        val still: ImageView = view.findViewById(R.id.gallery_film_still)
        private val empty: View = view.findViewById(R.id.gallery_film_empty)
        private val count: TextView = view.findViewById(R.id.gallery_film_count)
        fun bind(row: Row) {
            val entry = row.movie!!
            val movie = entry.movieResult
            val title = movie.title ?: movie.name.orEmpty()
            val path = movie.posterPath
            val fullPath = when {
                path.isNullOrBlank() -> null
                path.startsWith("http") || path.startsWith("content://") || path.count { it == '/' } > 1 -> path
                else -> Constants.TMDB_POSTER_IMAGE_BASE_URL_W342 + path
            }
            poster.loadImage(fullPath)
            poster.contentDescription = itemView.context.getString(R.string.gallery_poster_description, title)
            poster.setOnClickListener { onPosterClick(movie) }
            poster.setOnLongClickListener { onLongClick(poster, movie); true }
            val cover = row.images.firstOrNull()
            if (cover != null) still.loadImage(cover.imagePath)
            else { com.bumptech.glide.Glide.with(still).clear(still); still.setImageDrawable(null) }
            still.contentDescription = if (cover == null) itemView.context.getString(R.string.add_still)
                else itemView.context.getString(R.string.gallery_still_description, title)
            still.setOnClickListener { if (cover == null) onAddStill(entry.id) else onStillClick(still, cover) }
            empty.visibility = if (cover == null) View.VISIBLE else View.GONE
            // Leave the empty area clean; the whole right side can add its first image.
            count.visibility = if (row.images.size > 1) View.VISIBLE else View.GONE
            count.text = itemView.context.getString(R.string.gallery_image_count, 1, row.images.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == 1) MonthHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_month, parent, false))
        else FilmHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_film, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = getItem(position)
        if (row.movie != null) (holder as FilmHolder).bind(row)
        else {
            val label = holder.itemView as TextView
            label.text = row.month?.let { month ->
                val locale = label.resources.configuration.locales[0] ?: Locale.getDefault()
                val date = SimpleDateFormat("yyyy-MM", Locale.ROOT).parse(month)!!
                val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "yMMMM")
                SimpleDateFormat(pattern, locale).format(date)
            } ?: label.context.getString(R.string.gallery_undated)
        }
    }
    private object RowsDiff : DiffUtil.ItemCallback<Row>() {
        override fun areItemsTheSame(old: Row, new: Row) =
            if (old.movie == null && new.movie == null) old.month == new.month
            else old.movie?.id == new.movie?.id && old.movie != null && new.movie != null
        override fun areContentsTheSame(old: Row, new: Row) = old == new
    }
}
