/* Based on Seenema, Copyright (C) 2026 Sitavi. SPDX-License-Identifier: GPL-3.0-or-later */
package sitavi.seenema

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun HomeScreen(vm:AppViewModel,nav:NavController){
    val context=LocalContext.current
    var active by rememberSaveable{mutableStateOf(false)}
    var query by rememberSaveable{mutableStateOf("")}
    var timeline by rememberSaveable{mutableStateOf(true)}
    var menu by remember{mutableStateOf(false)}
    var manual by remember{mutableStateOf(false)}
    var importZip by remember{mutableStateOf(true)}
    var searching by remember{mutableStateOf(false)}
    var failed by remember{mutableStateOf(false)}
    var results by remember{mutableStateOf(listOf<SuggestItem>())}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->if(uri!=null)vm.importFile(uri,importZip)}
    LaunchedEffect(query){
        if(query.isBlank()){results=emptyList();searching=false;failed=false;return@LaunchedEffect}
        searching=true;failed=false;delay(350)
        try{results=Imdb.search(query).filter{it.isTitle&&it.mediaType=="movie"}}
        catch(e:CancellationException){throw e}
        catch(e:Exception){failed=true;results=emptyList()}
        finally{searching=false}
    }
    val records=vm.records()
    Scaffold(
        snackbarHost={SnackbarHost(vm.snackbarHostState)},
        floatingActionButton={if(!active&&vm.loaded)FloatingActionButton(onClick={if(!vm.busy)manual=true}){Icon(Icons.Default.Add,"添加电影")}},
        bottomBar={if(!active)NavigationBar{
            NavigationBarItem(selected=timeline,onClick={timeline=true},icon={Icon(Icons.Default.DateRange,null)},label={Text("时间线")})
            NavigationBarItem(selected=!timeline,onClick={timeline=false},icon={Icon(painterResource(R.drawable.ic_logo),null)},label={Text("海报")})
        }}
    ){padding->Column(Modifier.fillMaxSize().padding(padding)){
        if(!active)Row(Modifier.fillMaxWidth().padding(start=20.dp,end=6.dp,top=8.dp,bottom=4.dp),verticalAlignment=Alignment.CenterVertically){
            Icon(painterResource(R.drawable.ic_logo),null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(30.dp))
            Spacer(Modifier.width(10.dp));Text("Seenema",style=MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.weight(1f));Box{
                IconButton(onClick={menu=true},enabled=vm.loaded&&!vm.busy){Icon(Icons.Default.MoreVert,"菜单")}
                DropdownMenu(expanded=menu,onDismissRequest={menu=false}){
                    DropdownMenuItem(text={Text("导出完整备份")},onClick={menu=false;vm.exportBackup(context)})
                    DropdownMenuItem(text={Text("导入完整备份")},onClick={menu=false;importZip=true;picker.launch("application/zip")})
                    DropdownMenuItem(text={Text("导入旧版 CSV")},onClick={menu=false;importZip=false;picker.launch("*/*")})
                    HorizontalDivider();DropdownMenuItem(text={Text("关于")},onClick={menu=false;nav.navigate("about")})
                }
            }
        }
        SearchBar(query=query,onQueryChange={query=it},onSearch={},active=active,onActiveChange={active=it},placeholder={Text("搜索电影")},leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={if(active)IconButton(onClick={if(query.isBlank())active=false else query=""}){Icon(Icons.Default.Close,"关闭搜索")}},windowInsets=WindowInsets(0.dp),modifier=Modifier.align(Alignment.CenterHorizontally).then(if(active)Modifier else Modifier.fillMaxWidth().padding(horizontal=16.dp))){
            if(searching)LinearProgressIndicator(Modifier.fillMaxWidth())
            if(failed)Text("无法连接 IMDb，可以使用右下角按钮手动添加。",Modifier.padding(20.dp),color=MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn{items(results,key={it.id}){r->ListItem(
                headlineContent={Text(r.l)},supportingContent={Text(r.year)},leadingContent={Poster(r.i?.imageUrl,Modifier.width(48.dp).height(72.dp))},
                modifier=Modifier.clickable{val item=SeenItem(r.id,"movie",r.l,r.year,r.i?.imageUrl.orEmpty(),Status.SEEN,0,"",java.time.LocalDateTime.now().toString());vm.preview(item);nav.navigate("title/movie/${r.id}")}
            )}}
        }
        if(vm.busy)LinearProgressIndicator(Modifier.fillMaxWidth().padding(top=8.dp))
        when{
            !vm.loaded->Centered{CircularProgressIndicator()}
            records.isEmpty()->Centered(Modifier.weight(1f)){Text("暂无观影记录",color=MaterialTheme.colorScheme.onSurfaceVariant)}
            else->{
                val groups=if(timeline)records.groupBy{it.addedAt.take(7)} else mapOf("" to records)
                LazyVerticalGrid(columns=GridCells.Adaptive(100.dp),contentPadding=PaddingValues(start=16.dp,end=16.dp,top=18.dp,bottom=90.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
                    groups.forEach{(month,list)->
                        if(timeline)item(key="month-$month",span={GridItemSpan(maxLineSpan)}){
                            Text(runCatching{YearMonth.parse(month).format(DateTimeFormatter.ofPattern("yyyy · MMM"))}.getOrDefault(month),style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=8.dp,bottom=2.dp))
                        }
                        items(list,key={DiaryAssets.key(it)}){item->Column(Modifier.clickable{nav.navigate("title/${item.mediaType}/${item.id}")}){
                            Box(Modifier.fillMaxWidth().aspectRatio(2f/3f).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)){
                                Icon(painterResource(R.drawable.ic_logo),null,tint=MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=.3f),modifier=Modifier.align(Alignment.Center).size(40.dp))
                                AsyncImage(model=vm.poster(item),contentDescription=item.title,contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
                            }
                            Text(item.title,maxLines=1,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.labelMedium,modifier=Modifier.padding(top=6.dp))
                            Text(if(timeline)item.addedAt.take(10) else item.year,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        }}
                    }
                }
            }
        }
    }}
    if(manual){
        var title by rememberSaveable{mutableStateOf("")};var year by rememberSaveable{mutableStateOf("")}
        AlertDialog(onDismissRequest={manual=false},title={Text("添加电影")},text={Column{
            OutlinedTextField(value=title,onValueChange={title=it.take(160)},label={Text("片名")},singleLine=true)
            Spacer(Modifier.height(12.dp));OutlinedTextField(value=year,onValueChange={year=it.filter(Char::isDigit).take(4)},label={Text("年份（选填）")},singleLine=true)
        }},confirmButton={TextButton(enabled=title.isNotBlank(),onClick={val item=vm.manual(title,year);vm.preview(item);vm.markSeen(item);manual=false;nav.navigate("title/movie/${item.id}")}){Text("添加")}},dismissButton={TextButton(onClick={manual=false}){Text("取消")}})
    }
}
