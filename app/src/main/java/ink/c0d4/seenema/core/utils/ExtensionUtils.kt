package ink.c0d4.seenema.core.utils

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import ink.c0d4.seenema.R

fun ImageView.loadImage(
    url: Any?,
    placeholder: Drawable? = null,
    listener: RequestListener<Drawable>? = null,
    skipCache: Boolean = false,
) {
    try {
        Glide.with(this.context)
            .load(url)
            .timeout(60000)
            .placeholder(placeholder)
            .error(R.drawable.poster_bg)
            .addListener(listener)
            .skipMemoryCache(skipCache)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(this)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    }
}