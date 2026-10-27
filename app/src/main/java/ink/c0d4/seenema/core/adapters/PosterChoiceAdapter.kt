package ink.c0d4.seenema.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ink.c0d4.seenema.R
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.core.utils.loadImage

class PosterChoiceAdapter(
    private val onPosterClick: (String) -> Unit
) : ListAdapter<String, PosterChoiceAdapter.ViewHolder>(PosterDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.poster_choice_image)

        fun bind(path: String) {
            image.loadImage(Constants.TMDB_IMAGE_BASE_URL_W500.plus(path))
            itemView.setOnClickListener { onPosterClick(path) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_poster_choice, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PosterDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}
