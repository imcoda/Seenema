package ink.c0d4.seenema.core.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.ChipGroup
import ink.c0d4.seenema.R
import ink.c0d4.seenema.data.network.ApiClient
import ink.c0d4.seenema.domain.model.MovieResult
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaShareEntryPoint {
    fun getApiClient(): ApiClient
}

class MediaSharePosterPicker(fragment: Fragment) {
    private var pendingResult: ((Uri?) -> Unit)? = null
    private val launcher = fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val result = pendingResult
        pendingResult = null
        result?.invoke(uri)
    }

    fun launch(onResult: (Uri?) -> Unit) {
        pendingResult = onResult
        launcher.launch("image/*")
    }
}


enum class MediaShareTemplate(@param:LayoutRes val layoutResId: Int) {
    EDITORIAL_POSTER(R.layout.layout_sharable_media_editorial),
    CHARACTER_POSTER(R.layout.layout_sharable_media_character)
}

object MediaShareImageGenerator {
    internal fun createPosterView(
        context: Context, movie: MovieResult, heroBitmap: Bitmap?,
        template: MediaShareTemplate, directorName: String? = null
    ): View {
        val view = LayoutInflater.from(context).inflate(template.layoutResId, null)
        view.findViewById<TextView>(R.id.media_title).text = movie.title ?: movie.name.orEmpty()
        val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
        val year = movie.releaseDate?.take(4)?.takeIf { it.length == 4 && it.all(Char::isDigit) }
        val rating = movie.voteAverage?.takeIf { it > 0 }?.let { "★ " + String.format(locale, "%.1f", it) }
        view.findViewById<TextView>(R.id.media_metadata).apply {
            text = listOfNotNull(year, rating).joinToString("  ·  ")
            visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
        }
        view.findViewById<TextView>(R.id.media_director_label).apply {
            text = directorName?.takeIf(String::isNotBlank)?.let { context.getString(R.string.share_director, it) }
            visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        val image = view.findViewById<ImageView>(R.id.media_hero_image)
        if (heroBitmap != null) {
            image.setImageBitmap(heroBitmap)
            if (template == MediaShareTemplate.EDITORIAL_POSTER) {
                image.layoutParams.height = (984.0 * heroBitmap.height / heroBitmap.width).toInt().coerceIn(320, 2160)
            }
        } else image.visibility = View.GONE
        view.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, 1080, view.measuredHeight)
        return view
    }

    fun generateImage(context: Context, movie: MovieResult, heroBitmap: Bitmap?,
                      template: MediaShareTemplate, directorName: String? = null): Bitmap {
        val view = createPosterView(context, movie, heroBitmap, template, directorName)
        return Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
            view.draw(Canvas(this))
        }
    }
}

internal fun configureMediaShareTemplates(view: View) {
    view.findViewById<View>(R.id.chip_template_classic).visibility = View.GONE
    view.findViewById<View>(R.id.chip_template_minimal).visibility = View.GONE
    view.findViewById<View>(R.id.chip_media_editorial).visibility = View.VISIBLE
    view.findViewById<View>(R.id.chip_media_character).visibility = View.VISIBLE
    view.findViewById<View>(R.id.share_custom_poster_btn).visibility = View.VISIBLE
    view.findViewById<ChipGroup>(R.id.share_template_chip_group).check(R.id.chip_media_editorial)
}

fun shareMediaCard(fragment: Fragment, movie: MovieResult, posterPicker: MediaSharePosterPicker) {
    val ctx = fragment.context ?: return

    showToast(ctx, "Generating sharable image...")

    fragment.lifecycleScope.launch(Dispatchers.IO) {
        try {
            val movieId = movie.id ?: -1
            val isTv = movie.mediaType.equals("tv", ignoreCase = true)

            // 1. Fetch Hero Image
            val imagePath = movie.posterPath ?: movie.backdropPath
            val isLocal = imagePath != null && (imagePath.startsWith("content://") || imagePath.count { it == '/' } > 1)
            val fullPath = if (isLocal) imagePath else Constants.TMDB_IMAGE_BASE_URL_W780.plus(imagePath)

            val heroBitmap = try {
                Glide.with(fragment)
                    .asBitmap()
                    .load(fullPath)
                    .submit()
                    .get()
            } catch (e: Exception) {
                null
            }

            // Fetch director information when credits are available.
            var directorName: String? = null

            if (movieId != -1) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        ctx.applicationContext,
                        MediaShareEntryPoint::class.java
                    )
                    val apiClient = entryPoint.getApiClient()

                    // Credits / Director
                    val castResponse = if (isTv) {
                        apiClient.fetchTVCastApiCall(movieId)
                    } else {
                        apiClient.fetchMovieCastApiCall(movieId)
                    }
                    if (castResponse.isSuccessful) {
                        val castBody = castResponse.body()
                        val crewList = castBody?.crew ?: emptyList()
                        val director = crewList.firstOrNull { it.job.equals("Director", ignoreCase = true) }
                            ?: crewList.firstOrNull { it.department.equals("Directing", ignoreCase = true) }
                        directorName = director?.name
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            withContext(Dispatchers.Main) {
                showMediaSharePreviewBottomSheet(
                    fragment,
                    movie,
                    heroBitmap,
                    directorName,
                    posterPicker
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showToast(ctx, "Failed to generate image")
            }
        }
    }
}

private fun showMediaSharePreviewBottomSheet(
    fragment: Fragment,
    movie: MovieResult,
    heroBitmap: Bitmap?,
    directorName: String?,
    posterPicker: MediaSharePosterPicker
) {
    val ctx = fragment.context ?: return
    val dialog = BottomSheetDialog(ctx, R.style.SheetDialog)
    dialog.setOnShowListener { dialogInterface ->
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    val view = fragment.layoutInflater.inflate(R.layout.bottom_sheet_share_preview, null)
    val previewImg = view.findViewById<ImageView>(R.id.share_preview_image)
    val chipGroup = view.findViewById<ChipGroup>(R.id.share_template_chip_group)
    val downloadBtn = view.findViewById<View>(R.id.share_download_btn)
    val shareBtn = view.findViewById<View>(R.id.share_now_btn)
    val cancelBtn = view.findViewById<View>(R.id.share_cancel_btn)
    val customPosterBtn = view.findViewById<View>(R.id.share_custom_poster_btn)

    configureMediaShareTemplates(view)

    var selectedTemplate = MediaShareTemplate.EDITORIAL_POSTER
    var selectedHeroBitmap = heroBitmap
    var currentBitmap = MediaShareImageGenerator.generateImage(
        ctx, movie, selectedHeroBitmap, selectedTemplate, directorName
    )

    fun displayPreview() {
        previewImg.setImageBitmap(currentBitmap)
        previewImg.post {
            if (previewImg.width > 0) {
                val height = (previewImg.width.toDouble() * currentBitmap.height / currentBitmap.width).toInt()
                previewImg.layoutParams = previewImg.layoutParams.apply {
                    this.height = height.coerceAtMost((ctx.resources.displayMetrics.heightPixels * 0.48).toInt())
                }
            }
        }
    }
    fun updatePreview() {
        currentBitmap = MediaShareImageGenerator.generateImage(
            ctx, movie, selectedHeroBitmap, selectedTemplate, directorName
        )
        displayPreview()
    }
    displayPreview()

    chipGroup?.setOnCheckedStateChangeListener { _, checkedIds ->
        selectedTemplate = if (R.id.chip_media_character in checkedIds) {
            MediaShareTemplate.CHARACTER_POSTER
        } else {
            MediaShareTemplate.EDITORIAL_POSTER
        }
        updatePreview()
    }

    customPosterBtn.setOnClickListener {
        posterPicker.launch { uri ->
            if (uri != null) {
                fragment.lifecycleScope.launch(Dispatchers.IO) {
                    val customPoster = try {
                        Glide.with(fragment)
                            .asBitmap()
                            .load(uri)
                            .fitCenter()
                            .submit(1080, 1600)
                            .get()
                    } catch (e: Exception) {
                        null
                    }

                    withContext(Dispatchers.Main) {
                        if (!dialog.isShowing) return@withContext
                        if (customPoster == null) {
                            showToast(ctx, "Unable to load selected poster")
                        } else {
                            selectedHeroBitmap = customPoster
                            updatePreview()
                        }
                    }
                }
            }
        }
    }

    downloadBtn.setOnClickListener {
        saveMediaBitmapToGallery(ctx, currentBitmap)
        dialog.dismiss()
    }

    shareBtn.setOnClickListener {
        saveAndShareMediaBitmap(fragment, currentBitmap)
        dialog.dismiss()
    }

    cancelBtn.setOnClickListener {
        dialog.dismiss()
    }

    dialog.setContentView(view)
    dialog.show()
}

private fun saveMediaBitmapToGallery(context: Context, bitmap: Bitmap) {
    val filename = "Seenema_Share_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Seenema")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    try {
        uri?.let {
            val stream = resolver.openOutputStream(it)
            if (stream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, values, null, null)
                }
                showToast(context, "Saved to Gallery!")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        showToast(context, "Failed to save image")
    }
}

private fun saveAndShareMediaBitmap(fragment: Fragment, bitmap: Bitmap) {
    val context = fragment.context ?: return
    try {
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "movie_share.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        fragment.startActivity(Intent.createChooser(intent, "Share Movie"))
    } catch (e: Exception) {
        e.printStackTrace()
        showToast(context, "Failed to share image")
    }
}
