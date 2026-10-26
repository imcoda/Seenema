package com.shalenmathew.movieflix.diary

import android.os.Bundle
import android.os.Looper
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import com.shalenmathew.movieflix.MovieFlixApp
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.backup.BackupData
import com.shalenmathew.movieflix.core.backup.BackupWorker
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.DiaryDate
import com.shalenmathew.movieflix.core.utils.GalleryTimeline
import com.shalenmathew.movieflix.data.local_storage.MovieDao
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.presentation.MainActivity
import com.shalenmathew.movieflix.presentation.movie_details.MovieDetailsFragment
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
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = MovieFlixApp::class)
@LooperMode(LooperMode.Mode.PAUSED)
class WatchedMonthTest {
    private fun idleUntil(check: () -> Boolean) {
        repeat(100) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            if (check()) return
            Thread.sleep(20)
        }
        assertTrue("Timed out waiting for the saved month", check())
    }

    @Test fun savingWatchedMonthReordersTimelineAndPreservesMovieNoteImagesAndBackup() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity = controller.get()
            val dao = EntryPointAccessors.fromApplication(activity.application,
                BackupWorker.BackupWorkerEntryPoint::class.java).movieDatabase().dao
            val film = MovieResult(id = -51, title = "A memory", mediaType = "movie")
            val original = FavouritesEntity(-51, film, "Keep this note", "2026-10-26 12:00:00")
            val image = PersonalGalleryEntity(mediaId = -51, imagePath = "/local/still.jpg")
            runBlocking {
                dao.insertFavMovie(original)
                dao.insertGalleryImage(image)
                dao.insertFavMovie(FavouritesEntity(-52, film.copy(id = -52), insertedAt = "2025-12-01"))
            }
            val host = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            host.navController.navigate(R.id.movieDetailsFragment, Bundle().apply {
                putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(film))
            })
            host.childFragmentManager.executePendingTransactions()
            val detail = host.childFragmentManager.fragments.filterIsInstance<MovieDetailsFragment>().last()
            val row = detail.binding.fragmentMovieDetailsWatchedMonth
            idleUntil { row.visibility == android.view.View.VISIBLE }
            assertEquals("Watched: October 2026", row.text.toString())
            row.performClick()
            detail.childFragmentManager.executePendingTransactions()
            var picker = detail.childFragmentManager.findFragmentByTag(WatchedMonthDialogFragment.REQUEST) as WatchedMonthDialogFragment
            var dialog = picker.requireDialog() as AlertDialog
            val year = dialog.findViewById<NumberPicker>(R.id.watched_year_picker)!!
            val month = dialog.findViewById<NumberPicker>(R.id.watched_month_picker)!!
            assertEquals(2026, year.value)
            assertEquals(10, month.value)
            year.value = 2024
            month.value = 2
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            idleUntil { runBlocking { dao.getFavMovieById(-51)?.insertedAt } == "2024-02-01 12:00:00" }
            idleUntil { row.text.toString() == "Watched: February 2024" }
            detail.childFragmentManager.executePendingTransactions()

            val saved = runBlocking { dao.getFavMovieById(-51)!! }
            assertEquals(original.copy(insertedAt = "2024-02-01 12:00:00"), saved)
            val all = runBlocking { dao.getAllFavMoviesSync() }
            assertEquals(listOf("2025-12", "2024-02"), GalleryTimeline.sections(all).map { it.month })
            assertEquals(-51, GalleryTimeline.sections(all).last().movies.single().id)
            assertEquals("/local/still.jpg", runBlocking { dao.getAllGalleryImagesSync() }.single().imagePath)
            val backup = Gson().fromJson(Gson().toJson(BackupData(favorites = all)), BackupData::class.java)
            assertEquals(saved, backup.favorites.single { it.id == -51 })

            // Reopening uses the saved value; cancelling never overwrites it.
            row.performClick()
            detail.childFragmentManager.executePendingTransactions()
            picker = detail.childFragmentManager.findFragmentByTag(WatchedMonthDialogFragment.REQUEST) as WatchedMonthDialogFragment
            dialog = picker.requireDialog() as AlertDialog
            assertEquals(2024, dialog.findViewById<NumberPicker>(R.id.watched_year_picker)!!.value)
            assertEquals(2, dialog.findViewById<NumberPicker>(R.id.watched_month_picker)!!.value)
            dialog.findViewById<NumberPicker>(R.id.watched_month_picker)!!.value = 12
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertEquals(saved, runBlocking { dao.getFavMovieById(-51) })
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test @Config(qualifiers = "zh-rCN")
    fun undatedMovieCanChooseMonthAndPickerDraftSurvivesRecreation() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            var activity = controller.get()
            var fragment = WatchedMonthDialogFragment.newInstance(-7, null)
            fragment.show(activity.supportFragmentManager, "month")
            activity.supportFragmentManager.executePendingTransactions()
            var dialog = fragment.requireDialog() as AlertDialog
            assertEquals("年", dialog.findViewById<TextView>(R.id.watched_year_label)!!.text.toString())
            dialog.findViewById<NumberPicker>(R.id.watched_year_picker)!!.value = 2001
            dialog.findViewById<NumberPicker>(R.id.watched_month_picker)!!.value = 8
            controller.recreate()
            activity = controller.get()
            fragment = activity.supportFragmentManager.findFragmentByTag("month") as WatchedMonthDialogFragment
            dialog = fragment.requireDialog() as AlertDialog
            assertEquals(2001, dialog.findViewById<NumberPicker>(R.id.watched_year_picker)!!.value)
            assertEquals(8, dialog.findViewById<NumberPicker>(R.id.watched_month_picker)!!.value)
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test fun monthStorageAcceptsOnlyRealMonthsAndKeepsLegacyTimelineFormat() {
        assertEquals("2024-02", GalleryTimeline.monthKey(DiaryDate.watchedMonthTimestamp(2024, 2)))
        assertEquals("2000-01", GalleryTimeline.monthKey(DiaryDate.watchedMonthTimestamp(2000, 1)))
        assertThrows(IllegalArgumentException::class.java) { DiaryDate.watchedMonthTimestamp(2024, 13) }
        assertThrows(IllegalArgumentException::class.java) { DiaryDate.watchedMonthTimestamp(2024, 0) }
        assertThrows(IllegalArgumentException::class.java) { DiaryDate.watchedMonthTimestamp(0, 1) }
    }
}
