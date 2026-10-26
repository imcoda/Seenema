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
    @Test fun galleryRetainsNativeCardsAndDetailsWithoutDuplicateSearch(){
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity=controller.get();Shadows.shadowOf(Looper.getMainLooper()).idle()
            val host=activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            assertEquals(R.id.libraryFragment,host.navController.currentDestination?.id)
            val nav=activity.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            assertEquals(3,nav.menu.size())
            assertNull(nav.menu.findItem(R.id.homeFragment))
            assertEquals("Gallery",nav.menu.findItem(R.id.libraryFragment).title.toString())
            val title=activity.findViewById<android.widget.TextView>(R.id.fragment_fav_tv)
            assertEquals("Gallery",title.text.toString())
            assertFalse(title.hasOnClickListeners())
            assertEquals(0,activity.resources.getIdentifier("diary_add","id",activity.packageName))
            val library=host.childFragmentManager.primaryNavigationFragment!!
            for(name in listOf("tabLayout","fragmentLibrarySearchEt","fragmentLibrarySearchIconBtn")){
                val id=activity.resources.getIdentifier(name,"id",activity.packageName)
                assertNull(library.requireView().findViewById<android.view.View>(id))
            }
            val movie=com.shalenmathew.movieflix.domain.model.MovieResult(id=-1,title="离线电影",releaseDate="1999",mediaType="movie")
            host.navController.navigate(R.id.movieDetailsFragment,android.os.Bundle().apply{putString(com.shalenmathew.movieflix.core.utils.Constants.MEDIA_SEND_REQUEST_KEY,com.google.gson.Gson().toJson(movie))})
            host.childFragmentManager.executePendingTransactions();Shadows.shadowOf(Looper.getMainLooper()).idle()
            val detail=host.childFragmentManager.fragments.filterIsInstance<com.shalenmathew.movieflix.presentation.movie_details.MovieDetailsFragment>().last()
            assertEquals("离线电影",detail.binding.fragmentMovieDetailsTitle.text.toString())
            assertEquals("1999",detail.binding.fragmentMovieDetailsYear.text.toString())
            assertEquals(android.view.View.GONE,detail.binding.fragmentMovieDetailsPlayBtn.visibility)
            assertEquals("Add to gallery",detail.binding.fragmentMovieDetailsFavBtn.contentDescription.toString())
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
            val moreId=activity.resources.getIdentifier("fragment_movie_details_more_btn","id",activity.packageName)
            assertNull(detail.requireView().findViewById<android.view.View>(moreId))
            host.navController.popBackStack(R.id.libraryFragment,false)
            host.childFragmentManager.executePendingTransactions()
            val grid=activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.fragment_fav_rv)
            for(attempt in 0 until 100){
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                if(grid.adapter?.itemCount==2)break
                Thread.sleep(20)
            }
            assertEquals(2,grid.adapter!!.itemCount)
            val manager=grid.layoutManager as androidx.recyclerview.widget.GridLayoutManager
            assertEquals(manager.spanCount,manager.spanSizeLookup.getSpanSize(0))
            assertEquals(1,manager.spanSizeLookup.getSpanSize(1))
            host.navController.navigate(R.id.settingsFragment)
            host.childFragmentManager.executePendingTransactions()
            val settings=host.childFragmentManager.primaryNavigationFragment!!
            val footerId=activity.resources.getIdentifier("tv_footer","id",activity.packageName)
            assertNull(settings.requireView().findViewById<android.view.View>(footerId))
        }finally{controller.pause().stop().destroy()}
    }
}
