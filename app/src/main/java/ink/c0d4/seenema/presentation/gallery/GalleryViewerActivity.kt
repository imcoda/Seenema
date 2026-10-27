package ink.c0d4.seenema.presentation.gallery

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ink.c0d4.seenema.R
import ink.c0d4.seenema.data.local_storage.entity.PersonalGalleryEntity
import ink.c0d4.seenema.presentation.viewmodels.FavMovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryViewerActivity : AppCompatActivity() {
    private val model: FavMovieViewModel by viewModels()
    private lateinit var pager: ViewPager2
    private lateinit var toolbar: View
    private lateinit var counter: TextView
    private lateinit var photos: PhotosAdapter
    private var selectedId = 0
    private var initialId = 0
    private var chromeVisible = true
    private var dataLoaded = false
    private val releaseTransition = Runnable { startPostponedEnterTransition() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK))
        window.sharedElementEnterTransition = TransitionSet().apply {
            addTransition(ChangeBounds()); addTransition(ChangeTransform()); addTransition(ChangeImageTransform())
            duration = 280; interpolator = DecelerateInterpolator()
        }
        window.sharedElementReturnTransition = window.sharedElementEnterTransition.clone()
        window.enterTransition = Fade().setDuration(180)
        window.returnTransition = Fade().setDuration(180)
        postponeEnterTransition()
        setContentView(R.layout.activity_gallery_viewer)
        pager = findViewById(R.id.gallery_viewer_pager)
        toolbar = findViewById(R.id.gallery_viewer_toolbar)
        counter = findViewById(R.id.gallery_viewer_count)
        val root = findViewById<View>(R.id.gallery_viewer_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        initialId = intent.getIntExtra(IMAGE_ID, 0)
        selectedId = savedInstanceState?.getInt(IMAGE_ID) ?: initialId
        photos = PhotosAdapter()
        pager.adapter = photos
        pager.offscreenPageLimit = 1
        pager.setPageTransformer { page, position ->
            page.alpha = 1f - kotlin.math.abs(position).coerceAtMost(1f) * 0.25f
        }
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                currentImage()?.let { sharedElements?.put(SHARED_STILL, it) }
            }
        })
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (dataLoaded) selectedId = photos.currentList.getOrNull(position)?.id ?: selectedId
                counter.text = getString(R.string.gallery_image_count, position + 1, photos.itemCount)
            }
        })
        findViewById<View>(R.id.gallery_viewer_close).setOnClickListener { closeViewer() }
        findViewById<View>(R.id.gallery_viewer_delete).setOnClickListener {
            val image = photos.currentList.getOrNull(pager.currentItem) ?: return@setOnClickListener
            MaterialAlertDialogBuilder(this, R.style.TrackingAlertDialog)
                .setTitle(R.string.delete_gallery_image)
                .setMessage(R.string.delete_gallery_image_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.btn_delete) { _, _ -> model.deleteGalleryImage(image) }
                .show()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val image = currentImage()
                if (image != null && image.zoom > 1.01f) image.resetZoom() else closeViewer()
            }
        })
        val mediaId = intent.getIntExtra(MEDIA_ID, 0)
        intent.getStringExtra(IMAGE_PATH)?.let { path ->
            photos.submitList(listOf(PersonalGalleryEntity(initialId, mediaId, path)))
        }
        model.getGalleryImages(mediaId).observe(this) { images ->
            if (images.isEmpty()) { finish(); return@observe }
            val oldPosition = pager.currentItem
            val desiredId = selectedId
            dataLoaded = false
            photos.submitList(images) {
                val index = images.indexOfFirst { it.id == desiredId }.takeIf { it >= 0 }
                    ?: oldPosition.coerceAtMost(images.lastIndex)
                pager.setCurrentItem(index, false)
                selectedId = images[index].id
                dataLoaded = true
                counter.text = getString(R.string.gallery_image_count, index + 1, images.size)
            }
        }
        root.postDelayed(releaseTransition, 1000)
    }

    private fun currentImage() = (pager.getChildAt(0) as RecyclerView)
        .findViewHolderForAdapterPosition(pager.currentItem)?.itemView as? ZoomImageView

    private fun closeViewer() {
        if (selectedId == initialId) {
            finishAfterTransition()
        } else finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(IMAGE_ID, selectedId)
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        findViewById<View>(R.id.gallery_viewer_root)?.removeCallbacks(releaseTransition)
        super.onDestroy()
    }

    private inner class PhotosAdapter : ListAdapter<PersonalGalleryEntity, PhotoHolder>(PhotoDiff) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PhotoHolder(ZoomImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            contentDescription = getString(R.string.view_still)
            onImageTap = {
                chromeVisible = !chromeVisible
                toolbar.animate().alpha(if (chromeVisible) 1f else 0f).setDuration(160).withEndAction {
                    toolbar.visibility = if (chromeVisible) View.VISIBLE else View.INVISIBLE
                }.start()
                if (chromeVisible) toolbar.visibility = View.VISIBLE
            }
        })
        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val image = getItem(position)
            holder.image.transitionName = if (image.id == initialId) SHARED_STILL else null
            Glide.with(holder.image).load(image.imagePath).override(2560, 2560).fitCenter().dontAnimate()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        startPostponedEnterTransition(); return false
                    }
                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        holder.image.post { startPostponedEnterTransition() }; return false
                    }
                }).into(holder.image)
        }
        override fun onViewRecycled(holder: PhotoHolder) { Glide.with(holder.image).clear(holder.image) }
    }
    private class PhotoHolder(val image: ZoomImageView) : RecyclerView.ViewHolder(image)
    private object PhotoDiff : DiffUtil.ItemCallback<PersonalGalleryEntity>() {
        override fun areItemsTheSame(old: PersonalGalleryEntity, new: PersonalGalleryEntity) = old.id == new.id
        override fun areContentsTheSame(old: PersonalGalleryEntity, new: PersonalGalleryEntity) = old == new
    }
    companion object {
        const val MEDIA_ID = "gallery_media_id"
        const val IMAGE_ID = "gallery_image_id"
        const val IMAGE_PATH = "gallery_image_path"
        const val SHARED_STILL = "gallery_still_transition"
    }
}
