package ink.c0d4.seenema.diary

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import ink.c0d4.seenema.BuildConfig
import ink.c0d4.seenema.R
import ink.c0d4.seenema.SeenemaApp
import ink.c0d4.seenema.core.backup.BackupWorker
import ink.c0d4.seenema.core.backup.RestoreWorker
import ink.c0d4.seenema.core.utils.ZipHelper
import java.io.File
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = SeenemaApp::class)
class SeenemaIdentityTest {
    @Test fun launcherAndFileSharingUseSeenemaIdentityInBothLanguages() {
        val context = RuntimeEnvironment.getApplication() as SeenemaApp
        assertEquals("ink.c0d4.seenema", context.packageName)
        assertEquals("ink.c0d4.seenema", BuildConfig.APPLICATION_ID)
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PROVIDERS)
        assertTrue(info.providers!!.any { it.authority == "ink.c0d4.seenema.fileprovider" })
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        assertEquals(SeenemaApp::class.java.name, appInfo.className)
        assertEquals("Seenema", context.packageManager.getApplicationLabel(appInfo).toString())
        for (locale in listOf(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE)) {
            val config = Configuration(context.resources.configuration).apply { setLocale(locale) }
            assertEquals("Seenema", context.createConfigurationContext(config).getString(R.string.app_name))
        }
    }

    @Test fun legacyBackupImportsNotesWatchedMonthAndImagesIntoNewPackage() = runBlocking {
        val context = RuntimeEnvironment.getApplication() as SeenemaApp
        val fixture = File(context.cacheDir, "legacy_backup_fixture").apply { mkdirs() }
        val oldFiles = "/data/user/0/legacy.app/files"
        val json = File(fixture, "backup.json").apply { writeText("""
            {"version":2,"backupTime":1,
             "favorites":[{"id":194,"movieResult":{"id":194,"title":"天使爱美丽","mediaType":"movie","posterPath":"$oldFiles/poster_194.jpg"},"personalNote":"记住的画面","insertedAt":"2024-02-01 12:00:00"}],
             "watchList":[],"scheduled":[],"series":[],"seasons":[],"episodes":[],"progress":[],"customLists":[],"customListMovies":[],
             "galleryImages":[{"id":1,"mediaId":194,"imagePath":"$oldFiles/gallery_194.jpg","addedAt":10}]}
        """.trimIndent()) }
        val still = File(fixture, "gallery_194.jpg").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val poster = File(fixture, "poster_194.jpg").apply { writeBytes(byteArrayOf(5, 6, 7)) }
        val zip = File(context.cacheDir, "legacy_backup.zip")
        ZipHelper.zip(listOf(json, still, poster), zip)
        val params = mock<WorkerParameters>()
        whenever(params.inputData).thenReturn(Data.Builder().putString("URI", Uri.fromFile(zip).toString()).build())
        assertEquals(ListenableWorker.Result.success(), RestoreWorker(context, params).doWork())
        val dao = EntryPointAccessors.fromApplication(context, BackupWorker.BackupWorkerEntryPoint::class.java)
            .movieDatabase().dao
        val restored = dao.getFavMovieById(194)!!
        assertEquals("记住的画面", restored.personalNote)
        assertEquals("2024-02-01 12:00:00", restored.insertedAt)
        assertEquals(File(context.filesDir, poster.name).path, restored.movieResult.posterPath)
        val image = dao.getAllGalleryImagesSync().single()
        assertEquals(File(context.filesDir, still.name).path, image.imagePath)
        assertArrayEquals(still.readBytes(), File(image.imagePath).readBytes())
        assertArrayEquals(poster.readBytes(), File(restored.movieResult.posterPath!!).readBytes())
    }
}
