package ink.c0d4.seenema.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ink.c0d4.seenema.R
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.core.utils.isNetworkAvailable
import ink.c0d4.seenema.core.utils.loadImage
import ink.c0d4.seenema.data.local_storage.entity.WatchListEntity
import ink.c0d4.seenema.databinding.ItemSmallListBinding
import ink.c0d4.seenema.domain.model.MovieResult

class WatchListAdapter(
    private var onPosterClick: ((movieResult: MovieResult) -> Unit),
    private var onLongClick: ((view: View, movieResult: MovieResult) -> Unit)
): ListAdapter<WatchListEntity, WatchListAdapter.ViewHolder>(DiffUtilCallback()) {
    
    private var scheduledMovieIds = setOf<Int>()

    fun updateScheduledMovies(scheduledIds: Set<Int>) {
        scheduledMovieIds = scheduledIds
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
       var binding: ItemSmallListBinding
        init {
            binding = ItemSmallListBinding.bind(itemView)
        }
fun bind(watchListEntity: WatchListEntity)=binding.apply{

    val item = watchListEntity.movieResult
    val posterPath = item.posterPath
    val isLocal = posterPath != null && (posterPath.startsWith("content://") || posterPath.count { it == '/' } > 1)
    
    if (isLocal) {
        itemListPoster.loadImage(posterPath)
    } else {
        itemListPoster.loadImage(Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(posterPath))
    }
    
    itemListRatingTxt.text = String.format("%.1f", item.voteAverage)

    // Show schedule icon if movie is scheduled
    itemListScheduleIcon.visibility = if (scheduledMovieIds.contains(item.id)) {
        android.view.View.VISIBLE
    } else {
        android.view.View.GONE
    }

    root.setOnClickListener(){
        if (isNetworkAvailable(root.context)){
            onPosterClick(item)
        }else{
            Toast.makeText(root.context, root.context.getString(R.string.msg_internet_required), Toast.LENGTH_SHORT).show()
        }
    }

    root.setOnLongClickListener {
        onLongClick(root, item)
        true
    }
}
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_small_list,parent,false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class DiffUtilCallback : DiffUtil.ItemCallback<WatchListEntity>() {
        override fun areItemsTheSame(oldItem: WatchListEntity, newItem: WatchListEntity): Boolean =
            oldItem == newItem
        override fun areContentsTheSame(
            oldItem: WatchListEntity,
            newItem: WatchListEntity
        ): Boolean =
            oldItem == newItem
    }
}