package com.shalenmathew.movieflix.diary

import android.os.Looper
import android.view.View
import android.widget.EditText
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shalenmathew.movieflix.MovieFlixApp
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.presentation.MainActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=MovieFlixApp::class,qualifiers="en-rUS")
@LooperMode(LooperMode.Mode.PAUSED)
class SearchKeyboardTest {
    @Test fun typedSearchRemainsReadableOnTheOriginalDarkBackground() {
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity=controller.get()
            val host=activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            host.navController.navigate(R.id.searchFragment)
            host.childFragmentManager.executePendingTransactions()
            val input=activity.findViewById<EditText>(R.id.fragment_search_et)
            input.setText("天使爱美丽 Amélie")
            assertEquals("天使爱美丽 Amélie",input.text.toString())
            assertTrue(ColorUtils.calculateContrast(input.currentTextColor,activity.getColor(R.color.gray_shade_1))>7)
            assertNotNull(input.textCursorDrawable)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun navigationDoesNotFloatAboveImeAndReturnsToScreenBottom() {
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup().visible()
        try {
            val activity=controller.get()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            val nav=activity.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            val root=nav.parent as View
            val host=activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            host.navController.navigate(R.id.searchFragment)
            host.childFragmentManager.executePendingTransactions()
            fun insets(keyboard:Boolean)=WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.systemBars(),Insets.of(0,24,0,24))
                .setInsets(WindowInsetsCompat.Type.ime(),if(keyboard)Insets.of(0,0,0,600) else Insets.NONE)
                .setVisible(WindowInsetsCompat.Type.ime(),keyboard).build()
            ViewCompat.dispatchApplyWindowInsets(root,insets(true))
            assertEquals(View.GONE,nav.visibility)
            host.navController.navigate(R.id.settingsFragment)
            host.childFragmentManager.executePendingTransactions()
            assertEquals(View.GONE,nav.visibility)
            ViewCompat.dispatchApplyWindowInsets(root,insets(false))
            root.measure(View.MeasureSpec.makeMeasureSpec(1080,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1920,View.MeasureSpec.EXACTLY))
            root.layout(0,0,1080,1920)
            assertEquals(View.VISIBLE,nav.visibility)
            assertEquals(root.height-root.paddingBottom,nav.bottom)
            assertEquals(0f,nav.translationY)
        } finally { controller.pause().stop().destroy() }
    }
}
