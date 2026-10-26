package com.shalenmathew.movieflix.core.adapters
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.DiaryDate
import com.shalenmathew.movieflix.core.utils.loadImage
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.databinding.ItemSmallListBinding
import com.shalenmathew.movieflix.domain.model.MovieResult
/** Reuses MovieFlix's original poster card; month headings are full-span rows. */
class DiaryAdapter(private val onPosterClick:(MovieResult)->Unit, private val onLongClick:(View,MovieResult)->Unit):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private data class Row(val month:String?=null,val film:FavouritesEntity?=null)
    private var rows=listOf<Row>();private var films=listOf<FavouritesEntity>()
    var timeline=true
        set(value){field=value;submitList(films)}
    fun isHeading(position:Int)=rows.getOrNull(position)?.month!=null
    fun updateScheduledMovies(ids:Set<Int>) {}
    fun submitList(list:List<FavouritesEntity>){
        films=list.sortedByDescending { it.insertedAt }
        rows=buildList { var previous:String?=null
            for(film in films){val month=DiaryDate.month(film.insertedAt);if(timeline&&month!=previous){add(Row(month=month));previous=month};add(Row(film=film))}
        };notifyDataSetChanged()
    }
    override fun getItemCount()=rows.size
    override fun getItemViewType(position:Int)=if(isHeading(position))1 else 0
    private class Holder(view:View):RecyclerView.ViewHolder(view)
    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):RecyclerView.ViewHolder {
        val view=if(viewType==1)TextView(parent.context).apply{setTextColor(Color.WHITE);textSize=18f;val d=resources.displayMetrics.density;setPadding((16*d).toInt(),(20*d).toInt(),0,(12*d).toInt())}
        else LayoutInflater.from(parent.context).inflate(R.layout.item_small_list,parent,false)
        return Holder(view)
    }
    override fun onBindViewHolder(holder:RecyclerView.ViewHolder,position:Int){
        val row=rows[position]
        if(row.month!=null){(holder.itemView as TextView).text=row.month;return}
        val film=row.film!!;val movie=film.movieResult;val binding=ItemSmallListBinding.bind(holder.itemView)
        val path=movie.posterPath;val local=path!=null&&(path.startsWith("content://")||path.count{it=='/'}>1)
        binding.itemListPoster.loadImage(if(path.isNullOrBlank())null else if(local)path else Constants.TMDB_POSTER_IMAGE_BASE_URL_W342+path)
        binding.itemListPoster.contentDescription=movie.title?:movie.name
        binding.itemListRatingTxt.text=film.insertedAt?.take(10).orEmpty()
        binding.itemListScheduleIcon.visibility=View.GONE
        binding.root.setOnClickListener{onPosterClick(movie)}
        binding.root.setOnLongClickListener{onLongClick(it,movie);true}
    }
}
