/* Based on Seenema, Copyright (C) 2026 Sitavi. SPDX-License-Identifier: GPL-3.0-or-later */
package sitavi.seenema

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun TitleScreen(vm:AppViewModel,nav:NavController,type:String,id:String){
    var remote by remember(id){mutableStateOf<GqlTitle?>(null)}
    var error by remember(id){mutableStateOf(false)}
    var reload by remember(id){mutableIntStateOf(0)}
    var edit by remember{mutableStateOf(false)}
    var delete by remember{mutableStateOf(false)}
    var selectedPhoto by remember{mutableStateOf<File?>(null)}
    var deletePhoto by remember{mutableStateOf<File?>(null)}
    LaunchedEffect(id,reload){if(id.matches(Regex("tt[0-9]+"))){error=false;try{remote=Imdb.title(id)}catch(e:CancellationException){throw e}catch(e:Exception){error=true}}}
    val known=vm.detailItem(id,type)
    val d=remote
    val item=known?:d?.let{SeenItem(id,type,it.displayTitle,it.yearText,it.primaryImage?.url.orEmpty(),Status.SEEN,0,"",java.time.LocalDateTime.now().toString())}
    val saved=vm.itemOf(id,type)
    val photos=if(item!=null)vm.photos(item) else emptyList()
    val galleryPicker=rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()){uris->if(item!=null&&uris.isNotEmpty())vm.addPhotos(item,uris)}
    val posterPicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->if(item!=null&&uri!=null)vm.setPoster(item,uri)}
    Scaffold(snackbarHost={SnackbarHost(vm.snackbarHostState)},topBar={TopAppBar(title={},navigationIcon={IconButton(onClick={nav.popBackStack()}){Icon(Icons.AutoMirrored.Filled.ArrowBack,"返回")}},actions={
        if(saved!=null){IconButton(onClick={edit=true},enabled=!vm.busy){Icon(Icons.Default.Edit,"编辑记录")};IconButton(onClick={delete=true},enabled=!vm.busy){Icon(Icons.Default.Delete,"移除记录")}}
    })}){padding->
        if(item==null){Centered(Modifier.padding(padding)){if(error)Column(horizontalAlignment=Alignment.CenterHorizontally){Text("无法读取电影资料");TextButton(onClick={reload++}){Text("重试")}}else CircularProgressIndicator()}}
        else LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(bottom=28.dp)){
            item{Row(Modifier.padding(16.dp),verticalAlignment=Alignment.Top){
                AsyncImage(model=vm.poster(item),contentDescription="${item.title}海报",contentScale=ContentScale.Fit,modifier=Modifier.width(110.dp).height(165.dp).clickable(enabled=saved!=null&&!vm.busy){posterPicker.launch("image/*")})
                Column(Modifier.padding(start=16.dp)){
                    Text(d?.displayTitle?.ifBlank{null}?:item.title,style=MaterialTheme.typography.headlineSmall)
                    if(item.year.isNotBlank())Text(item.year,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    if(d?.ratingsSummary?.aggregateRating!=null)Text("IMDb ${d.ratingsSummary.aggregateRating}/10",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    if(saved!=null)Text(saved.addedAt.take(10),modifier=Modifier.padding(top=12.dp),style=MaterialTheme.typography.labelMedium)
                }
            }}
            if(vm.busy)item{LinearProgressIndicator(Modifier.fillMaxWidth())}
            if(saved==null)item{Button(onClick={vm.markSeen(item);edit=true},enabled=vm.loaded&&!vm.busy,modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp)){Text("标记已看")}}
            if(saved!=null){
                item{Column(Modifier.fillMaxWidth().padding(16.dp)){
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("个人笔记",style=MaterialTheme.typography.titleMedium);Spacer(Modifier.weight(1f));TextButton(onClick={edit=true},enabled=!vm.busy){Text("编辑")}}
                    if(saved.rating>0)Text("★".repeat(saved.rating),color=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(bottom=10.dp))
                    if(saved.note.isNotBlank())Text(saved.note,style=MaterialTheme.typography.bodyLarge)
                }}
                item{Row(Modifier.fillMaxWidth().padding(start=16.dp,end=8.dp),verticalAlignment=Alignment.CenterVertically){Text("个人剧照",style=MaterialTheme.typography.titleMedium);Spacer(Modifier.weight(1f));TextButton(onClick={galleryPicker.launch("image/*")},enabled=!vm.busy){Icon(Icons.Default.Add,null);Text("添加")}}}
                if(photos.isNotEmpty()){
                    item{AsyncImage(model=photos.first(),contentDescription="最记住的剧照",contentScale=ContentScale.Fit,modifier=Modifier.fillMaxWidth().heightIn(min=150.dp,max=420.dp).padding(horizontal=16.dp).clickable{selectedPhoto=photos.first()})}
                    item{LazyRow(contentPadding=PaddingValues(16.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){items(photos,key={it.name}){file->
                        Column{AsyncImage(model=file,contentDescription="剧照",contentScale=ContentScale.Crop,modifier=Modifier.width(130.dp).height(86.dp).clickable{selectedPhoto=file});TextButton(onClick={deletePhoto=file},enabled=!vm.busy){Text("移除")}}
                    }}}
                }
                item{TextButton(onClick={posterPicker.launch("image/*")},enabled=!vm.busy,modifier=Modifier.padding(horizontal=8.dp)){Text("更换海报")}}
            }
            val plot=d?.plot?.plotText?.plainText
            if(!plot.isNullOrBlank())item{Column(Modifier.padding(16.dp)){Text("简介",style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(10.dp));Text(plot,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
        }
    }
    if(edit&&item!=null){EditRecord(saved?:item,onDismiss={edit=false},onSave={note,date,rating->vm.update(item,note,date,rating);edit=false})}
    if(delete&&item!=null)AlertDialog(onDismissRequest={delete=false},title={Text("移除记录？")},confirmButton={TextButton(onClick={delete=false;vm.remove(item);nav.popBackStack()}){Text("移除")}},dismissButton={TextButton(onClick={delete=false}){Text("取消")}})
    val removing=deletePhoto
    if(removing!=null&&item!=null)AlertDialog(onDismissRequest={deletePhoto=null},title={Text("移除剧照？")},confirmButton={TextButton(onClick={deletePhoto=null;vm.removePhoto(item,removing)}){Text("移除")}},dismissButton={TextButton(onClick={deletePhoto=null}){Text("取消")}})
    val photo=selectedPhoto
    if(photo!=null)Dialog(onDismissRequest={selectedPhoto=null},properties=DialogProperties(usePlatformDefaultWidth=false)){
        Surface{Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){IconButton(onClick={selectedPhoto=null}){Icon(Icons.Default.Close,"关闭大图")}}
            AsyncImage(model=photo,contentDescription=item?.title,contentScale=ContentScale.Fit,modifier=Modifier.weight(1f).fillMaxWidth())
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                val n=photos.indexOf(photo);TextButton(enabled=n>0,onClick={selectedPhoto=photos[n-1]}){Text("上一张")};Text("${n+1} / ${photos.size}");TextButton(enabled=n<photos.lastIndex,onClick={selectedPhoto=photos[n+1]}){Text("下一张")}
            }
        }}
    }
}
@Composable private fun EditRecord(item:SeenItem,onDismiss:()->Unit,onSave:(String,String,Int)->Unit){
    var note by rememberSaveable(item.id){mutableStateOf(item.note)}
    var date by rememberSaveable(item.id){mutableStateOf(item.addedAt.take(10))}
    var rating by rememberSaveable(item.id){mutableIntStateOf(item.rating)}
    val valid=runCatching{LocalDate.parse(date)}.isSuccess
    AlertDialog(onDismissRequest=onDismiss,title={Text("观影记录")},text={Column{
        OutlinedTextField(value=date,onValueChange={date=it.take(10)},label={Text("观看日期 YYYY-MM-DD")},singleLine=true,isError=!valid)
        Spacer(Modifier.height(16.dp));StarRating(rating){rating=if(rating==it)0 else it}
        Spacer(Modifier.height(16.dp));OutlinedTextField(value=note,onValueChange={note=it.take(20000)},label={Text("个人笔记")},minLines=4,maxLines=8)
    }},confirmButton={TextButton(enabled=valid,onClick={onSave(note,date,rating)}){Text("保存")}},dismissButton={TextButton(onClick=onDismiss){Text("取消")}})
}
