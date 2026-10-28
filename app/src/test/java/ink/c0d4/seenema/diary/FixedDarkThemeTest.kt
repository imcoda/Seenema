package ink.c0d4.seenema.diary

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import ink.c0d4.seenema.R
import ink.c0d4.seenema.SeenemaApp
import ink.c0d4.seenema.presentation.MainActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = SeenemaApp::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FixedDarkThemeTest {
    private fun primaryText(context: Context): Int {
        val value = TypedValue()
        assertTrue(context.theme.resolveAttribute(android.R.attr.textColorPrimary, value, true))
        return if (value.resourceId != 0) ResourcesCompat.getColorStateList(context.resources, value.resourceId, context.theme)!!.defaultColor else value.data
    }

    private fun verifyFixedDarkAppearance() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
            fun verify() {
                val activity = controller.get()
                assertEquals(Configuration.UI_MODE_NIGHT_YES,
                    activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
                assertTrue(Color.red(primaryText(activity)) > 180)
                val dialog = BottomSheetDialog(activity, R.style.SheetDialog)
                assertTrue("Bottom sheets must use light text even when the system starts in day mode",
                    Color.red(primaryText(dialog.context)) > 180)
                dialog.dismiss()
            }
            verify()
            controller.recreate()
            verify()
            val changed = Configuration(controller.get().resources.configuration).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
            }
            controller.configurationChange(changed)
            assertTrue(Color.red(primaryText(controller.get())) > 180)
        } finally { controller.pause().stop().destroy() }
    }

    @Test @Config(qualifiers = "notnight")
    fun systemDayModeStillStartsAndRecreatesInDarkMode() = verifyFixedDarkAppearance()

    @Test @Config(qualifiers = "night")
    fun systemNightModeRetainsTheSameDarkAppearance() = verifyFixedDarkAppearance()
}
