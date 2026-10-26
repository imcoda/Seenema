package com.shalenmathew.movieflix.diary

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shalenmathew.movieflix.MovieFlixApp
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.FavAdapters
import com.shalenmathew.movieflix.core.adapters.GalleryTimelineAdapter
import com.shalenmathew.movieflix.core.backup.BackupWorker
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.presentation.MainActivity
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = MovieFlixApp::class, qualifiers = "en-rUS-w360dp-h760dp-xhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
class GalleryModeTest {
    private fun await(check: () -> Boolean) {
        repeat(100) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            if (check()) return
            Thread.sleep(20)
        }
        assertTrue(check())
    }
    @Test fun modeSwitchUsesOriginalThreeColumnRatedCardsAndPersistsAcrossRecreation() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            var activity = controller.get()
            val dao = EntryPointAccessors.fromApplication(activity.application,
                BackupWorker.BackupWorkerEntryPoint::class.java).movieDatabase().dao
            runBlocking {
                for (id in 1..6) dao.insertFavMovie(FavouritesEntity(id,
                    MovieResult(id = id, title = "Film $id", voteAverage = 8.0 + id / 10.0),
                    insertedAt = if (id < 5) "2026-10-01" else "2025-06-01"))
            }
            var list = activity.findViewById<RecyclerView>(R.id.fragment_fav_rv)
            await { list.adapter?.itemCount == 8 }
            val toggle = activity.findViewById<View>(R.id.gallery_view_toggle)
            toggle.performClick()
            assertTrue(list.adapter is FavAdapters)
            assertEquals(3, (list.layoutManager as GridLayoutManager).spanCount)
            await { list.adapter?.itemCount == 6 }
            val root = activity.findViewById<View>(android.R.id.content)
            root.measure(View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1520, View.MeasureSpec.EXACTLY))
            root.layout(0, 0, 720, 1520)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            val cards = (0..3).map { list.findViewHolderForAdapterPosition(it)!!.itemView }
            assertEquals(cards[0].top, cards[1].top)
            assertEquals(cards[0].top, cards[2].top)
            assertTrue(cards[3].top > cards[0].top)
            assertTrue(cards[0].left < cards[1].left && cards[1].left < cards[2].left)
            cards.forEach {
                assertEquals(R.id.item_list_container, it.id)
                assertNotNull(it.findViewById<View>(R.id.item_list_poster))
                val rating = it.findViewById<TextView>(R.id.item_list_rating_txt)
                assertEquals(View.VISIBLE, rating.visibility)
                assertTrue(rating.text.toString().matches(Regex("8\\.[1-6]")))
            }
            toggle.performClick()
            assertTrue(list.adapter is GalleryTimelineAdapter)
            assertEquals(8, list.adapter!!.itemCount)
            toggle.performClick()
            controller.recreate()
            activity = controller.get()
            list = activity.findViewById(R.id.fragment_fav_rv)
            await { list.adapter?.itemCount == 6 }
            assertTrue(list.adapter is FavAdapters)
            assertEquals(3, (list.layoutManager as GridLayoutManager).spanCount)
            assertTrue(activity.getSharedPreferences("gallery_display", Context.MODE_PRIVATE).getBoolean("compact", false))
            assertEquals(6, runBlocking { dao.getAllFavMoviesSync() }.size)
        } finally {
            controller.get().getSharedPreferences("gallery_display", Context.MODE_PRIVATE).edit().clear().commit()
            controller.pause().stop().destroy()
        }
    }

    @Test fun firstStillIsAtomicAndLegacyMultipleImagesArePreserved() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity = controller.get()
            val dao = EntryPointAccessors.fromApplication(activity.application,
                BackupWorker.BackupWorkerEntryPoint::class.java).movieDatabase().dao
            runBlocking {
                dao.insertFavMovie(FavouritesEntity(21, MovieResult(id = 21), "Note", "2024-05-01"))
                val first = PersonalGalleryEntity(mediaId = 21, imagePath = "/first.jpg")
                assertTrue(dao.insertFirstGalleryImage(first))
                assertFalse(dao.insertFirstGalleryImage(first.copy(imagePath = "/second.jpg")))
                assertEquals(1, dao.getGalleryImageCount(21))
                assertEquals("/first.jpg", dao.getAllGalleryImagesSync().single().imagePath)
                dao.deleteGalleryImage(dao.getAllGalleryImagesSync().single())
                assertTrue(dao.insertFirstGalleryImage(first.copy(imagePath = "/replacement.jpg")))
                assertEquals("Note", dao.getFavMovieById(21)!!.personalNote)
                dao.insertFavMovie(FavouritesEntity(22, MovieResult(id = 22), insertedAt = "2024-01-01"))
                dao.insertGalleryImage(PersonalGalleryEntity(mediaId = 22, imagePath = "/legacy-1.jpg"))
                dao.insertGalleryImage(PersonalGalleryEntity(mediaId = 22, imagePath = "/legacy-2.jpg"))
                assertFalse(dao.insertFirstGalleryImage(PersonalGalleryEntity(mediaId = 22, imagePath = "/extra.jpg")))
                assertEquals(2, dao.getGalleryImageCount(22))
                assertFalse(dao.insertFirstGalleryImage(PersonalGalleryEntity(mediaId = 999, imagePath = "/orphan.jpg")))
            }
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun emptyStillAreaLaunchesSingleImagePicker() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity = controller.get()
            val dao = EntryPointAccessors.fromApplication(activity.application,
                BackupWorker.BackupWorkerEntryPoint::class.java).movieDatabase().dao
            runBlocking { dao.insertFavMovie(FavouritesEntity(31, MovieResult(id = 31), insertedAt = "2026-10-01")) }
            val list = activity.findViewById<RecyclerView>(R.id.fragment_fav_rv)
            await { list.adapter?.itemCount == 2 }
            val adapter = list.adapter as GalleryTimelineAdapter
            val holder = adapter.onCreateViewHolder(list, 0) as GalleryTimelineAdapter.FilmHolder
            adapter.onBindViewHolder(holder, 1)
            holder.still.performClick()
            val intent = Shadows.shadowOf(activity).nextStartedActivityForResult.intent
            assertEquals(Intent.ACTION_GET_CONTENT, intent.action)
            assertEquals("image/*", intent.type)
            assertFalse(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
        } finally { controller.pause().stop().destroy() }
    }
}
