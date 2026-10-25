/* Based on Seenema, Copyright (C) 2026 Sitavi. SPDX-License-Identifier: GPL-3.0-or-later */
package sitavi.seenema

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SeenStore(app)
    private val assets = DiaryAssets(app)
    private val writes = Mutex()
    val snackbarHostState = SnackbarHostState()
    var seen by mutableStateOf(listOf<SeenItem>())
        private set
    var loaded by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    private var revision by mutableIntStateOf(0)
    init { viewModelScope.launch {
        try { seen = withContext(Dispatchers.IO) { store.load() } }
        catch (e: Exception) { message("读取记录失败：${e.message}") }
        loaded = true
    } }
    private val previews = mutableMapOf<String, SeenItem>()
    fun preview(item: SeenItem) { previews[DiaryAssets.key(item)] = item }
    fun detailItem(id: String, type: String) = itemOf(id,type) ?: previews["$type-$id"]
    fun itemOf(id: String, type: String) = seen.firstOrNull { it.id == id && it.mediaType == type }
    fun records() = seen.filter { it.status == Status.SEEN }.sortedByDescending { it.addedAt }
    fun photos(item: SeenItem): List<File> { revision; return assets.photos(DiaryAssets.key(item)) }
    fun poster(item: SeenItem): Any? { revision; return assets.poster(DiaryAssets.key(item)) ?: Imdb.thumb(item.posterUrl.ifBlank { null }, 500) }
    fun markSeen(item: SeenItem) {
        if (!loaded || busy) return
        if (itemOf(item.id,item.mediaType) == null) { seen = listOf(item.copy(status=Status.SEEN, addedAt=LocalDateTime.now().withNano(0).toString())) + seen; persist() }
        else { seen = seen.map { if(it.id==item.id && it.mediaType==item.mediaType) it.copy(status=Status.SEEN) else it }; persist() }
        cachePoster(item)
    }
    fun update(item: SeenItem, note: String, date: String, rating: Int) {
        if (busy) return
        val day = LocalDate.parse(date)
        val edited = (itemOf(item.id,item.mediaType) ?: item).copy(note=note,addedAt="${day}T12:00:00",rating=rating.coerceIn(0,5),status=Status.SEEN)
        seen = listOf(edited) + seen.filterNot { it.id==item.id && it.mediaType==item.mediaType }
        persist(); cachePoster(edited)
    }
    fun manual(title: String, year: String): SeenItem = SeenItem("local-${UUID.randomUUID()}","movie",title.trim(),year.trim(),"",Status.SEEN,0,"",LocalDateTime.now().withNano(0).toString())
    fun remove(item: SeenItem) {
        if (busy) return
        seen = seen.filterNot { it.id==item.id && it.mediaType==item.mediaType }; persist()
        viewModelScope.launch { if(snackbarHostState.showSnackbar("已移除 ${item.title}","撤销")==SnackbarResult.ActionPerformed) {
            if(itemOf(item.id,item.mediaType)==null) { seen=listOf(item)+seen; persist() }
        } }
    }
    private fun cachePoster(item: SeenItem) { viewModelScope.launch {
        runCatching { withContext(Dispatchers.IO) { assets.cachePoster(item) } }; revision++
    } }
    fun setPoster(item: SeenItem, uri: Uri) { work { assets.setPoster(DiaryAssets.key(item),uri) } }
    fun addPhotos(item: SeenItem, uris: List<Uri>) { work { assets.addPhotos(DiaryAssets.key(item),uris) } }
    fun removePhoto(item: SeenItem, file: File) {
        if(busy)return
        viewModelScope.launch {
            try {
            val old=withContext(Dispatchers.IO){assets.removePhoto(DiaryAssets.key(item),file)};revision++
            if(snackbarHostState.showSnackbar("已移除剧照","撤销")==SnackbarResult.ActionPerformed) {
                withContext(Dispatchers.IO){assets.restorePhotos(DiaryAssets.key(item),old)};revision++
            }
            } catch(e:Exception) { message("保存失败：${e.message}") }
        }
    }
    private fun work(block: () -> Unit) {
        if(busy)return;busy=true
        viewModelScope.launch { try { withContext(Dispatchers.IO) { block() };revision++ }
            catch(e: Exception){message(e.message?:"保存失败")};busy=false }
    }
    private fun persist() {
        val snapshot=seen
        viewModelScope.launch { try { writes.withLock { withContext(Dispatchers.IO) { store.save(snapshot) } } }
            catch(e: Exception){message("保存失败：${e.message}")} }
    }
    fun exportBackup(context: Context) {
        if(busy)return;busy=true;val snapshot=seen
        viewModelScope.launch {
            try { val file=withContext(Dispatchers.IO){assets.export(store.csv(snapshot))}
                val uri=FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",file)
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("application/zip").putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),"保存备份"))
            }catch(e: Exception){message("备份失败：${e.message}")};busy=false
        }
    }
    fun importFile(uri: Uri, zip: Boolean) {
        if(busy)return;busy=true
        val existing=seen.map{DiaryAssets.key(it)}.toSet()
        viewModelScope.launch {
            try {
                val fresh=withContext(Dispatchers.IO) {
                    var rows=listOf<SeenItem>()
                    fun parse(text:String):Set<String> {
                        require(text.startsWith("id,")){"不是 Seenema 备份"}
                        rows=store.parseItems(text).filter { runCatching{LocalDate.parse(it.addedAt.take(10))}.isSuccess && it.title.isNotBlank() && it.mediaType in setOf("movie","tv") && it.id.matches(Regex("tt[0-9]+|local-[a-f0-9-]{36}")) }.filterNot{DiaryAssets.key(it) in existing}.distinctBy{DiaryAssets.key(it)}
                        return rows.map{DiaryAssets.key(it)}.toSet()
                    }
                    if(zip)assets.importBackup(uri,::parse)
                    else getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader()?.use{parse(it.readText())}?:error("无法读取文件")
                    rows
                }
                seen=seen+fresh;persist();revision++;message("已导入 ${fresh.size} 条记录")
            }catch(e:Exception){message("导入失败：${e.message}")};busy=false
        }
    }
    private fun message(text:String){viewModelScope.launch{snackbarHostState.showSnackbar(text)}}
}
