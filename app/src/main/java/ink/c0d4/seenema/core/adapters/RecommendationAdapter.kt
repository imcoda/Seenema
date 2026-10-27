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
import ink.c0d4.seenema.databinding.ItemListBinding
import ink.c0d4.seenema.domain.model.MovieResult

class RecommendationAdapter(private val posterClick:(movieResult:MovieResult)->Unit):ListAdapter<MovieResult, RecommendationAdapter.ViewHolder>(DiffUtilCallback()) {

    private var scheduledMovieIds = setOf<Int>()

    fun updateScheduledMovies(scheduledIds: Set<Int>) {
        scheduledMovieIds = scheduledIds
        notifyDataSetChanged()
    }

  inner  class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
      var binding:ItemListBinding = ItemListBinding.bind(itemView)
      fun bind(movieResult: MovieResult){
          binding.apply {
              itemListRatingTxt.text = String.format("%.1f",movieResult.voteAverage)
              
              val posterPath = movieResult.posterPath
              val isLocal = posterPath != null && (posterPath.startsWith("content://") || posterPath.count { it == '/' } > 1)
              
              if (isLocal) {
                  itemListPoster.loadImage(posterPath)
              } else {
                  itemListPoster.loadImage(Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(posterPath))
              }

              // Show schedule icon if movie is scheduled
              itemListScheduleIcon.visibility = if (scheduledMovieIds.contains(movieResult.id)) {
                  View.VISIBLE
              } else {
                  View.GONE
              }

              root.setOnClickListener(){
                  posterClick(movieResult)
              }
          }
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}