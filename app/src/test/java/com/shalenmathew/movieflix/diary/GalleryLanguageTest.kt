package com.shalenmathew.movieflix.diary

import android.content.res.Configuration
import android.os.Looper
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import com.shalenmathew.movieflix.MovieFlixApp
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.GalleryTimelineAdapter
import com.shalenmathew.movieflix.core.utils.DataStoreReference
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.presentation.MainActivity
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=MovieFlixApp::class,qualifiers="en-rUS")
@LooperMode(LooperMode.Mode.PAUSED)
class GalleryLanguageTest {
    @Test fun chineseTranslatesScreensAndAlbumMonthHeadings() {
        val app=RuntimeEnvironment.getApplication()
        val config=Configuration(app.resources.configuration).apply{setLocale(Locale.SIMPLIFIED_CHINESE)}
        val context=app.createConfigurationContext(config)
        assertEquals("画廊",context.getString(R.string.gallery))
        assertEquals("加入画廊",context.getString(R.string.add_to_favorites))
        assertEquals("设置",context.getString(R.string.settings))
        assertEquals("应用语言",context.getString(R.string.change_language))
        val adapter=GalleryTimelineAdapter({}, {_,_->})
        adapter.submitList(listOf(FavouritesEntity(1,MovieResult(id=1,title="Film"),insertedAt="2026-10-26")))
        val holder=adapter.onCreateViewHolder(FrameLayout(context),1)
        adapter.onBindViewHolder(holder,0)
        assertEquals("2026年10月",(holder.itemView as TextView).text.toString())
    }

    @Test fun originalLanguagePickerIncludesChineseAndPersistsItsSelection() {
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity=controller.get()
            val host=activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            host.navController.navigate(R.id.languageFragment)
            host.childFragmentManager.executePendingTransactions()
            val list=activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_languages)
            for(attempt in 0 until 100){
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                if(list.adapter!=null)break
                Thread.sleep(20)
            }
            val adapter=list.adapter as com.shalenmathew.movieflix.core.adapters.LanguageAdapter
            assertEquals(6,adapter.itemCount)
            val holder=adapter.onCreateViewHolder(list,0)
            adapter.onBindViewHolder(holder,5)
            assertEquals("中文（简体）",holder.itemView.findViewById<TextView>(R.id.tv_language_name).text.toString())
            holder.itemView.performClick()
            var selected=""
            for(attempt in 0 until 100){
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                selected=runBlocking{DataStoreReference.getSelectedLanguage(activity).first()}
                if(selected=="zh-CN")break
                Thread.sleep(20)
            }
            assertEquals("zh-CN",selected)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertEquals("zh-CN",activity.getSystemService(android.app.LocaleManager::class.java).applicationLocales.toLanguageTags())
        } finally {
            controller.pause().stop().destroy()
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.getEmptyLocaleList())
        }
    }
}
