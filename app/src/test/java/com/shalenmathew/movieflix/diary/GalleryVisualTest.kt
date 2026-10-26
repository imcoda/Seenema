package com.shalenmathew.movieflix.diary

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Looper
import android.view.View
import android.widget.GridLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.shalenmathew.movieflix.MovieFlixApp
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.backup.BackupWorker
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.presentation.MainActivity
import com.shalenmathew.movieflix.presentation.movie_details.WatchedMonthDialogFragment
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = MovieFlixApp::class, qualifiers = "en-rUS-w360dp-h760dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class GalleryVisualTest {
    private fun capture(view: View, name: String) {
        System.getenv("MOVIEFLIX_PREVIEW_DIR")?.let { dir ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(dir).mkdirs()
            File(dir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
    @Test fun nativeGalleryAndMonthGridFitPhoneWidthWithoutClipping() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity = controller.get()
            fun fixture(name: String, width: Int, height: Int, color: Int): String {
                val file = File(activity.cacheDir, name)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(color)
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
                return file.path
            }
            val poster = fixture("poster-layout.png", 200, 300, Color.rgb(99, 65, 89))
            val still = fixture("still-layout.png", 800, 450, Color.rgb(36, 89, 88))
            val dao = EntryPointAccessors.fromApplication(activity.application,
                BackupWorker.BackupWorkerEntryPoint::class.java).movieDatabase().dao
            runBlocking {
                for (id in 1..3) {
                    dao.insertFavMovie(FavouritesEntity(-100 - id, MovieResult(id = -100 - id,
                        title = "Film $id", posterPath = poster), insertedAt = if (id == 3) "2025-05-01" else "2026-10-01"))
                    dao.insertGalleryImage(PersonalGalleryEntity(mediaId = -100 - id, imagePath = still))
                }
            }
            val root = activity.findViewById<View>(android.R.id.content)
            val list = activity.findViewById<RecyclerView>(R.id.fragment_fav_rv)
            repeat(100) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                root.measure(View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1520, View.MeasureSpec.EXACTLY))
                root.layout(0, 0, 720, 1520)
                if (list.adapter?.itemCount == 5 && list.childCount >= 3) return@repeat
                Thread.sleep(20)
            }
            assertEquals(5, list.adapter!!.itemCount)
            assertTrue(list.childCount >= 3)
            root.viewTreeObserver.dispatchOnPreDraw()
            for (attempt in 0 until 50) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                if (root.findViewById<android.widget.ImageView>(R.id.gallery_film_still).drawable is android.graphics.drawable.BitmapDrawable) break
                Thread.sleep(20)
            }
            capture(root, "MovieFlix-personal6-gallery-layout.png")
            val picker = WatchedMonthDialogFragment.newInstance(-101, "2026-10")
            picker.show(activity.supportFragmentManager, "visual-month")
            activity.supportFragmentManager.executePendingTransactions()
            val sheet = picker.requireView()
            sheet.measure(View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            sheet.layout(0, 0, 720, sheet.measuredHeight)
            val months = sheet.findViewById<GridLayout>(R.id.watched_month_grid)
            assertEquals(12, months.childCount)
            assertEquals("2026", sheet.findViewById<android.widget.TextView>(R.id.watched_year_value).text.toString())
            assertTrue(months.getChildAt(11).right <= months.width)
            assertTrue(sheet.findViewById<View>(R.id.watched_month_save).bottom <= sheet.height)
            assertTrue(months.findViewWithTag<MaterialButton>("month_10").isSelected)
            capture(sheet, "MovieFlix-personal6-month-sheet.png")
        } finally { controller.pause().stop().destroy() }
    }
}
