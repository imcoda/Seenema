package ink.c0d4.seenema.core.utils

import androidx.recyclerview.widget.DiffUtil
import ink.c0d4.seenema.domain.model.MovieResult

class DiffUtilCallback:DiffUtil.ItemCallback<MovieResult>() {

    override fun areItemsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean {
        return oldItem.id==newItem.id
    }

    override fun areContentsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean {
return oldItem==newItem
    }
}