package ink.c0d4.seenema.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ink.c0d4.seenema.R
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.core.utils.DiffUtilCallback
import ink.c0d4.seenema.core.utils.isNetworkAvailable
import ink.c0d4.seenema.core.utils.loadImage
import ink.c0d4.seenema.databinding.ItemTopMovieBinding
import ink.c0d4.seenema.domain.model.MovieResult

class TrendingMovieAdapter(
    private val onClick:(movie:MovieResult)-> Unit,
    private var onLongClick: ((view: View, movieResult: MovieResult) -> Unit)? = null
): ListAdapter<MovieResult,TrendingMovieAdapter.ViewHolder>(DiffUtilCallback()) {

   inner  class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){

         private val binding:ItemTopMovieBinding
         init {
             binding = ItemTopMovieBinding.bind(itemView)
         }
        fun bind(movie:MovieResult){
            binding.apply {
                val context = root.context
                movieNameText.text = movie.title ?: movie.name
                movieImage.loadImage(Constants.TMDB_IMAGE_BASE_URL_W500.plus(movie.backdropPath))

                root.setOnClickListener(){
                   if(isNetworkAvailable(context)){
                       onClick(movie)
                   }else{
                       Toast.makeText(context, context.getString(R.string.msg_check_internet), Toast.LENGTH_SHORT).show()
                   }
                }

                root.setOnLongClickListener {
                    onLongClick?.invoke(root, movie)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_movie,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}