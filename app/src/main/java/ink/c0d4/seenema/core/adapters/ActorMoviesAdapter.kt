package ink.c0d4.seenema.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ink.c0d4.seenema.R
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.core.utils.DiffUtilCallback
import ink.c0d4.seenema.core.utils.loadImage
import ink.c0d4.seenema.databinding.ItemSmallListBinding
import ink.c0d4.seenema.domain.model.MovieResult

class ActorMoviesAdapter(
    private val onMovieClick: (MovieResult) -> Unit
) : ListAdapter<MovieResult, ActorMoviesAdapter.ViewHolder>(DiffUtilCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemSmallListBinding = ItemSmallListBinding.bind(itemView)

        fun bind(movie: MovieResult) {
            binding.apply {
                itemListRatingTxt.text = String.format("%.1f", movie.voteAverage ?: 0.0)
                itemListPoster.loadImage(
                    Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(movie.posterPath)
                )

                root.setOnClickListener {
                    onMovieClick(movie)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_small_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
