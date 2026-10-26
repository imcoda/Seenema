package com.shalenmathew.movieflix.diary
import android.os.Looper
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.MovieFlixApp
import com.shalenmathew.movieflix.presentation.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.NavHostFragment
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=MovieFlixApp::class)
@LooperMode(LooperMode.Mode.PAUSED)
class DiaryStartupTest {
    @Test fun launchesOriginalMovieFlixLibraryWithoutApiKey(){
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity=controller.get();Shadows.shadowOf(Looper.getMainLooper()).idle()
            val host=activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            assertEquals(R.id.libraryFragment,host.navController.currentDestination?.id)
            val nav=activity.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            assertEquals(3,nav.menu.size())
            assertNull(nav.menu.findItem(R.id.homeFragment))
            assertEquals("Library",nav.menu.findItem(R.id.libraryFragment).title.toString())
            val title=activity.findViewById<android.widget.TextView>(R.id.fragment_fav_tv)
            assertEquals("Favourites",title.text.toString())
            assertFalse(title.hasOnClickListeners())
            assertEquals(0,activity.resources.getIdentifier("diary_add","id",activity.packageName))
            val tabs=activity.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)
            assertEquals(android.view.View.VISIBLE,tabs.visibility)
            assertEquals(1,tabs.tabCount)
            val movie=com.shalenmathew.movieflix.domain.model.MovieResult(id=-1,title="离线电影",releaseDate="1999",mediaType="movie")
            host.navController.navigate(R.id.movieDetailsFragment,android.os.Bundle().apply{putString(com.shalenmathew.movieflix.core.utils.Constants.MEDIA_SEND_REQUEST_KEY,com.google.gson.Gson().toJson(movie))})
            host.childFragmentManager.executePendingTransactions();Shadows.shadowOf(Looper.getMainLooper()).idle()
            val detail=host.childFragmentManager.fragments.filterIsInstance<com.shalenmathew.movieflix.presentation.movie_details.MovieDetailsFragment>().last()
            assertEquals("离线电影",detail.binding.fragmentMovieDetailsTitle.text.toString())
            assertEquals("1999",detail.binding.fragmentMovieDetailsYear.text.toString())
            assertEquals(android.view.View.GONE,detail.binding.fragmentMovieDetailsPlayBtn.visibility)
            detail.binding.fragmentMovieDetailsFavBtn.performClick()
            val dao=dagger.hilt.android.EntryPointAccessors.fromApplication(activity.application,com.shalenmathew.movieflix.core.backup.BackupWorker.BackupWorkerEntryPoint::class.java).movieDatabase().dao
            var watched:String?=null
            for(attempt in 0 until 100){
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                watched=kotlinx.coroutines.runBlocking{dao.getFavMovieById(-1)?.insertedAt}
                if(watched==com.shalenmathew.movieflix.core.utils.DiaryDate.today()+" 12:00:00")break
                Thread.sleep(20)
            }
            assertEquals(com.shalenmathew.movieflix.core.utils.DiaryDate.today(),watched?.take(10))
            detail.binding.fragmentMovieDetailsMoreBtn.performClick()
            val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog()
            assertNotNull(dialog.findViewById<android.view.View>(R.id.more_options_change_poster_item))
            assertEquals(android.view.View.GONE,dialog.findViewById<android.view.View>(R.id.more_options_schedule_item).visibility)
            assertEquals(android.view.View.GONE,dialog.findViewById<android.view.View>(R.id.more_options_collection_item).visibility)
            dialog.dismiss()
        }finally{controller.pause().stop().destroy()}
    }
}
