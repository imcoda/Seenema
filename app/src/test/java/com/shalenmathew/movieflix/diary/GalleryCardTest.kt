package com.shalenmathew.movieflix.diary

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.shalenmathew.movieflix.MovieFlixApp
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.GalleryTimelineAdapter
import com.shalenmathew.movieflix.core.backup.BackupWorker
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.presentation.MainActivity
import com.shalenmathew.movieflix.presentation.gallery.GalleryViewerActivity
import com.shalenmathew.movieflix.presentation.gallery.ZoomImageView
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = MovieFlixApp::class, qualifiers = "en-rUS")
@LooperMode(LooperMode.Mode.PAUSED)
class GalleryCardTest {
    private fun await(check: () -> Boolean) {
        repeat(100) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            if (check()) return
            Thread.sleep(20)
        }
        assertTrue(check())
    }

    @Test fun fullWidthCardSeparatesPosterStillAndUploadAndUpdatesExistingImages() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val context = controller.get()
            val movie = MovieResult(id = 9, title = "Film", posterPath = "/official.jpg")
            var posterId = 0; var stillId = 0; var addId = 0
            val adapter = GalleryTimelineAdapter({ posterId = it.id!! }, { _, _ -> },
                { _, image -> stillId = image.id }, { addId = it })
            adapter.submitMovies(listOf(FavouritesEntity(9, movie, insertedAt = "2026-10-01")))
            val holder = adapter.onCreateViewHolder(FrameLayout(context), 0) as GalleryTimelineAdapter.FilmHolder
            adapter.onBindViewHolder(holder, 1)
            val card = holder.itemView
            card.measure(View.MeasureSpec.makeMeasureSpec(360, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            card.layout(0, 0, card.measuredWidth, card.measuredHeight)
            assertTrue(card.height > 100)
            assertTrue(holder.poster.width < holder.still.width)
            assertTrue(holder.poster.right <= card.findViewById<View>(R.id.gallery_film_still_container).left)
            assertEquals(holder.poster.height, holder.still.height)
            holder.poster.performClick()
            assertEquals(9, posterId)
            holder.still.performClick()
            assertEquals(9, addId)
            assertEquals(0, stillId)
            val old = PersonalGalleryEntity(11, 9, "/private-old.jpg", 10)
            val new = PersonalGalleryEntity(12, 9, "/private-new.jpg", 20)
            adapter.submitImages(listOf(old, new))
            await { adapter.currentList.last().images.size == 2 }
            adapter.onBindViewHolder(holder, 1)
            holder.still.performClick()
            assertEquals(12, stillId)
            assertEquals("1 / 2", card.findViewById<TextView>(R.id.gallery_film_count).text.toString())
            assertEquals(View.GONE, card.findViewById<View>(R.id.gallery_film_empty).visibility)
            card.findViewById<View>(R.id.gallery_film_add).performClick()
            assertEquals(9, addId)
            assertEquals("/official.jpg", adapter.currentList.last().movie!!.movieResult.posterPath)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun fullScreenViewerLoadsAllSavedStillsAndKeepsPageOnRecreation() {
        val main = Robolectric.buildActivity(MainActivity::class.java).setup()
        val context = main.get()
        val dao = EntryPointAccessors.fromApplication(context.application,
            BackupWorker.BackupWorkerEntryPoint::class.java).movieDatabase().dao
        val image = java.io.File(context.filesDir, "gallery_test.png").apply {
            outputStream().use { Bitmap.createBitmap(100, 60, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        runBlocking {
            dao.insertFavMovie(FavouritesEntity(-71, MovieResult(id = -71, title = "Memory"), "Note", "2023-01-01"))
            dao.insertGalleryImage(PersonalGalleryEntity(71, -71, image.path, 30))
            dao.insertGalleryImage(PersonalGalleryEntity(72, -71, image.path, 20))
        }
        val intent = Intent(context, GalleryViewerActivity::class.java).apply {
            putExtra(GalleryViewerActivity.MEDIA_ID, -71)
            putExtra(GalleryViewerActivity.IMAGE_ID, 71)
            putExtra(GalleryViewerActivity.IMAGE_PATH, image.path)
        }
        val viewer = Robolectric.buildActivity(GalleryViewerActivity::class.java, intent).setup()
        try {
            var activity = viewer.get()
            var pager = activity.findViewById<ViewPager2>(R.id.gallery_viewer_pager)
            await { pager.adapter?.itemCount == 2 }
            pager.setCurrentItem(1, false)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertEquals("2 / 2", activity.findViewById<TextView>(R.id.gallery_viewer_count).text.toString())
            viewer.recreate()
            activity = viewer.get()
            pager = activity.findViewById(R.id.gallery_viewer_pager)
            await { pager.adapter?.itemCount == 2 && pager.currentItem == 1 }
            assertEquals("Note", runBlocking { dao.getFavMovieById(-71) }?.personalNote)
            assertEquals(2, runBlocking { dao.getAllGalleryImagesSync() }.size)
            assertNotNull(activity.findViewById<View>(R.id.gallery_viewer_delete))
            activity.findViewById<View>(R.id.gallery_viewer_delete).performClick()
            (org.robolectric.shadows.ShadowDialog.getLatestDialog() as androidx.appcompat.app.AlertDialog)
                .getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick()
            await { pager.adapter?.itemCount == 1 }
            assertEquals(71, runBlocking { dao.getAllGalleryImagesSync() }.single().id)
            assertEquals("Note", runBlocking { dao.getFavMovieById(-71) }?.personalNote)
            activity.findViewById<View>(R.id.gallery_viewer_delete).performClick()
            (org.robolectric.shadows.ShadowDialog.getLatestDialog() as androidx.appcompat.app.AlertDialog)
                .getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick()
            await { activity.isFinishing }
            assertTrue(runBlocking { dao.getAllGalleryImagesSync() }.isEmpty())
            assertNotNull(runBlocking { dao.getFavMovieById(-71) })
        } finally {
            viewer.pause().stop().destroy()
            main.pause().stop().destroy()
        }
    }

    @Test fun doubleTapZoomAndResetKeepImageWithinItsAllowedBounds() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val view = ZoomImageView(controller.get())
            view.setImageDrawable(BitmapDrawable(controller.get().resources, Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888)))
            view.layout(0, 0, 400, 400)
            fun touch(action: Int, time: Long) {
                val event = MotionEvent.obtain(0, time, action, 200f, 200f, 0)
                view.onTouchEvent(event); event.recycle()
            }
            touch(MotionEvent.ACTION_DOWN, 0); touch(MotionEvent.ACTION_UP, 40)
            touch(MotionEvent.ACTION_DOWN, 100); touch(MotionEvent.ACTION_UP, 140)
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
            assertTrue(view.zoom > 2f)
            assertTrue(view.zoom <= 4f)
            view.resetZoom()
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
            assertEquals(1f, view.zoom, 0.01f)
            val rect = android.graphics.RectF(0f, 0f, 600f, 400f)
            view.imageMatrix.mapRect(rect)
            assertEquals(400f, rect.width(), 0.01f)
            assertEquals(200f, rect.centerY(), 0.01f)
        } finally { controller.pause().stop().destroy() }
    }
}
