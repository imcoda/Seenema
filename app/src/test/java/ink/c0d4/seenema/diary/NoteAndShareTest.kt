package ink.c0d4.seenema.diary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.EntryPointAccessors
import ink.c0d4.seenema.R
import ink.c0d4.seenema.SeenemaApp
import ink.c0d4.seenema.core.backup.BackupWorker
import ink.c0d4.seenema.core.utils.Constants
import ink.c0d4.seenema.core.utils.MediaShareImageGenerator
import ink.c0d4.seenema.core.utils.MediaShareTemplate
import ink.c0d4.seenema.core.utils.configureMediaShareTemplates
import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity
import ink.c0d4.seenema.domain.model.MovieResult
import ink.c0d4.seenema.presentation.MainActivity
import ink.c0d4.seenema.presentation.movie_details.MovieDetailsFragment
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
@Config(sdk = [35], application = SeenemaApp::class, qualifiers = "zh-rCN-w360dp-h760dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class NoteAndShareTest {
    private fun await(check: () -> Boolean) {
        repeat(100) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            if (check()) return
            Thread.sleep(20)
        }
        assertTrue(check())
    }

    @Test fun noteHintRemainsReadableOnFocusTypingClearingAndSaving() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity = controller.get()
            val dao = EntryPointAccessors.fromApplication(activity.application,
                BackupWorker.BackupWorkerEntryPoint::class.java).movieDatabase().dao
            val film = MovieResult(id = -81, title = "一段回忆", mediaType = "movie")
            runBlocking { dao.insertFavMovie(FavouritesEntity(-81, film, insertedAt = "2024-02-01")) }
            val host = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            host.navController.navigate(R.id.movieDetailsFragment, Bundle().apply {
                putString(Constants.MEDIA_SEND_REQUEST_KEY, com.google.gson.Gson().toJson(film))
            })
            host.childFragmentManager.executePendingTransactions()
            val detail = host.childFragmentManager.fragments.filterIsInstance<MovieDetailsFragment>().last()
            await { detail.binding.fragmentMovieDetailsPersonalNoteBtn.visibility == View.VISIBLE }
            detail.binding.fragmentMovieDetailsPersonalNoteBtn.performClick()
            val editor = detail.binding.fragmentMovieDetailsPersonalNoteEditText
            assertEquals(activity.getString(R.string.enter_your_note), editor.hint.toString())
            assertFalse(detail.binding.fragmentMovieDetailsPersonalNoteTextInputLayout.isHintEnabled)
            for (focused in listOf(false, true)) {
                if (focused) editor.requestFocus() else editor.clearFocus()
                assertTrue(ColorUtils.calculateContrast(editor.currentHintTextColor, Color.rgb(34, 34, 39)) >= 4.5)
                assertTrue(ColorUtils.calculateContrast(editor.currentTextColor, Color.rgb(34, 34, 39)) >= 4.5)
            }
            editor.setText("记住的画面")
            editor.onEditorAction(EditorInfo.IME_ACTION_DONE)
            await { runBlocking { dao.getFavMovieById(-81)?.personalNote } == "记住的画面" }
            detail.binding.fragmentMovieDetailsPersonalNote.performClick()
            assertEquals("记住的画面", editor.text.toString())
            editor.setText("")
            assertEquals(activity.getString(R.string.enter_your_note), editor.hint.toString())
            assertTrue(ColorUtils.calculateContrast(editor.currentHintTextColor, Color.rgb(34, 34, 39)) >= 4.5)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun bothShareChoicesRemainReadableInSelectedAndUnselectedStates() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity = controller.get()
            val view = activity.layoutInflater.inflate(R.layout.bottom_sheet_share_preview, null)
            configureMediaShareTemplates(view)
            val choices = listOf(R.id.chip_media_editorial, R.id.chip_media_character).map { view.findViewById<Chip>(it) }
            assertEquals(listOf("海报", "电影卡片"), choices.map { it.text.toString() })
            for (choice in choices) {
                choice.performClick()
                assertTrue(choice.isChecked)
                for (chip in choices) {
                    val background = chip.chipBackgroundColor!!.getColorForState(chip.drawableState, Color.BLACK)
                    assertTrue("Share labels need readable contrast in both states",
                        ColorUtils.calculateContrast(chip.currentTextColor, background) >= 4.5)
                }
            }
        } finally { controller.pause().stop().destroy() }
    }

    private fun posterFixture() = Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888).apply {
        val canvas = Canvas(this)
        val paint = Paint()
        paint.color = Color.RED; canvas.drawRect(0f, 0f, 200f, 150f, paint)
        paint.color = Color.BLUE; canvas.drawRect(0f, 150f, 200f, 300f, paint)
    }

    @Test fun exportsKeepCompletePostersAndLongTitlesSeparateFromImages() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity = controller.get()
            val film = MovieResult(title = "天使爱美丽 Amélie — Le Fabuleux Destin d’Amélie Poulain",
                releaseDate = "2001-04-25", voteAverage = 7.9)
            val fixture = posterFixture()
            for (template in MediaShareTemplate.entries) {
                val view = MediaShareImageGenerator.createPosterView(activity, film, fixture, template, "让-皮埃尔·热内") as ViewGroup
                val title = view.findViewById<TextView>(R.id.media_title)
                assertEquals(film.title, title.text.toString())
                assertEquals(title.text.length, title.layout.getLineEnd(title.layout.lineCount - 1))
                for (line in 0 until title.lineCount) assertEquals(0, title.layout.getEllipsisCount(line))
                val image = view.findViewById<ImageView>(R.id.media_hero_image)
                assertEquals(ImageView.ScaleType.FIT_CENTER, image.scaleType)
                val imageBounds = Rect().also { image.getDrawingRect(it); view.offsetDescendantRectToMyCoords(image, it) }
                val titleBounds = Rect().also { title.getDrawingRect(it); view.offsetDescendantRectToMyCoords(title, it) }
                assertFalse("Text must not cover the poster", Rect.intersects(imageBounds, titleBounds))
                val exported = MediaShareImageGenerator.generateImage(activity, film, fixture, template, "让-皮埃尔·热内")
                assertEquals(1080, exported.width)
                assertEquals(Color.RED, exported.getPixel(imageBounds.left + 12, imageBounds.top + 12))
                assertEquals(Color.BLUE, exported.getPixel(imageBounds.right - 12, imageBounds.bottom - 12))
                System.getenv("SEENEMA_PREVIEW_DIR")?.let { dir ->
                    val actual = System.getenv("SEENEMA_POSTER_FIXTURE")?.let(BitmapFactory::decodeFile) ?: fixture
                    val preview = MediaShareImageGenerator.generateImage(activity, film, actual, template, "让-皮埃尔·热内")
                    File(dir).mkdirs()
                    File(dir, "Seenema-share-${template.name.lowercase()}.png").outputStream().use {
                        preview.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    preview.recycle()
                }
                exported.recycle()
            }
            fixture.recycle()
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun missingMetadataDoesNotInventProvidersDatesOrCredits() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            for (template in MediaShareTemplate.entries) {
                val view = MediaShareImageGenerator.createPosterView(controller.get(), MovieResult(title = "未知电影"), null, template)
                assertEquals(View.GONE, view.findViewById<View>(R.id.media_metadata).visibility)
                assertEquals(View.GONE, view.findViewById<View>(R.id.media_director_label).visibility)
                assertEquals("Seenema", view.findViewById<TextView>(R.id.media_app_branding).text.toString())
            }
        } finally { controller.pause().stop().destroy() }
    }
}
