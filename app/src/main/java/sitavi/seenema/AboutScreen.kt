/* Based on Seenema, Copyright (C) 2026 Sitavi. SPDX-License-Identifier: GPL-3.0-or-later */
package sitavi.seenema
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun AboutScreen(nav:NavController){val context=LocalContext.current
    Scaffold(topBar={TopAppBar(title={Text("关于")},navigationIcon={IconButton(onClick={nav.popBackStack()}){Icon(Icons.AutoMirrored.Filled.ArrowBack,"返回")}})}){padding->Column(Modifier.padding(padding).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
        Text("Seenema",style=MaterialTheme.typography.headlineMedium)
        Text(BuildConfig.VERSION_NAME)
        Text("基于 Sitavi / Seenema，GPL-3.0-or-later。电影资料与海报来自 IMDb。")
        Text("完整备份包含记录、海报和剧照。导入时合并新记录，不覆盖已有电影。")
        Text("卸载 App 会删除本机数据，请先导出备份。")
        TextButton(onClick={context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/imcoda/seenema")))}){Text("源码")}
        TextButton(onClick={context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/Sitavi/Seenema-movie-tracker/blob/main/LICENSE")))}){Text("开源许可")}
    }}
}
