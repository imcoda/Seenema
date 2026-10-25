/* Based on Seenema, Copyright (C) 2026 Sitavi. SPDX-License-Identifier: GPL-3.0-or-later */
package sitavi.seenema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){installSplashScreen();super.onCreate(savedInstanceState);enableEdgeToEdge();setContent{MovieTrackerTheme{App()}}}
}
@Composable private fun App(){
    val vm:AppViewModel=viewModel();val nav=rememberNavController()
    NavHost(nav,startDestination="home"){
        composable("home"){HomeScreen(vm,nav)}
        composable("title/{type}/{id}"){entry->TitleScreen(vm,nav,entry.arguments?.getString("type")?:"movie",entry.arguments?.getString("id")?:"")}
        composable("about"){AboutScreen(nav)}
    }
}
