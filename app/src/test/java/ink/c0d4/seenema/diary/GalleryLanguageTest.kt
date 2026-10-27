package ink.c0d4.seenema.diary

import android.content.res.Configuration
import android.os.Looper
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import ink.c0d4.seenema.SeenemaApp
import ink.c0d4.seenema.R
import ink.c0d4.seenema.core.adapters.GalleryTimelineAdapter
import ink.c0d4.seenema.core.utils.DataStoreReference
import ink.c0d4.seenema.data.local_storage.entity.FavouritesEntity
import ink.c0d4.seenema.domain.model.MovieResult
import ink.c0d4.seenema.presentation.MainActivity
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
@Config(sdk=[35],application=SeenemaApp::class,qualifiers="en-rUS")
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
        adapter.submitMovies(listOf(FavouritesEntity(1,MovieResult(id=1,title="Film"),insertedAt="2026-10-26")))
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
            val adapter=list.adapter as ink.c0d4.seenema.core.adapters.LanguageAdapter
            assertEquals(2,adapter.itemCount)
            val holder=adapter.onCreateViewHolder(list,0)
            adapter.onBindViewHolder(holder,1)
            assertEquals("中文（简体）",holder.itemView.findViewById<TextView>(R.id.tv_language_name).text.toString())
            holder.itemView.performClick()
            var selected=""
            for(attempt in 0 until 100){
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                selected=runBlocking{DataStoreReference.getSelectedLanguage(activity).first()}
                if(selected=="zh-CN" && activity.getSystemService(android.app.LocaleManager::class.java)
                        .applicationLocales.toLanguageTags()=="zh-CN")break
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

    @Test @Config(qualifiers="zh-rCN")
    fun firstLaunchFollowsChineseSystemAndPickerIgnoresStaleEnglishPreference() {
        val app=RuntimeEnvironment.getApplication()
        runBlocking { DataStoreReference.setSelectedLanguage(app,"en-US") }
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity=controller.get()
            assertEquals("画廊",activity.getString(R.string.gallery))
            val host=activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            host.navController.navigate(R.id.languageFragment)
            host.childFragmentManager.executePendingTransactions()
            val list=activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_languages)
            for(attempt in 0 until 100){
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                if(list.adapter!=null)break
                Thread.sleep(20)
            }
            val adapter=list.adapter as ink.c0d4.seenema.core.adapters.LanguageAdapter
            assertEquals(2,adapter.itemCount)
            val english=adapter.onCreateViewHolder(list,0)
            adapter.onBindViewHolder(english,0)
            assertEquals(android.view.View.GONE,english.itemView.findViewById<android.view.View>(R.id.iv_selected).visibility)
            val chinese=adapter.onCreateViewHolder(list,0)
            adapter.onBindViewHolder(chinese,1)
            assertEquals(android.view.View.VISIBLE,chinese.itemView.findViewById<android.view.View>(R.id.iv_selected).visibility)
            assertEquals("zh-CN",runBlocking{DataStoreReference.getSelectedLanguage(activity).first()})
        } finally { controller.pause().stop().destroy() }
    }
}
