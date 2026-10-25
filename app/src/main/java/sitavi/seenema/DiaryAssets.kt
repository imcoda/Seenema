/* Based on Seenema, Copyright (C) 2026 Sitavi. SPDX-License-Identifier: GPL-3.0-or-later */
package sitavi.seenema

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.AtomicFile
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Private, durable copies: never depend on picker URI grants or the original photo. */
class DiaryAssets(private val context: Context) {
    private val root = File(context.filesDir, "diary-assets").apply { mkdirs() }
    private val indexFile = AtomicFile(File(root, "gallery.json"))
    private var index = if (indexFile.baseFile.exists()) JSONObject(indexFile.readFully().toString(Charsets.UTF_8)) else JSONObject()
    private val client = OkHttpClient.Builder().callTimeout(java.time.Duration.ofSeconds(20)).build()
    companion object {
        private const val MAX_IMAGE = 20L * 1024 * 1024
        private const val MAX_BACKUP = 512L * 1024 * 1024
        private val validName = Regex("[a-f0-9-]{36}\\.(jpg|png|webp)")
        fun key(item: SeenItem) = "${item.mediaType}-${item.id}"
    }
    @Synchronized private fun entry(key: String): JSONObject = index.optJSONObject(key) ?: JSONObject().put("images", JSONArray())
    @Synchronized fun photos(key: String): List<File> = entry(key).optJSONArray("images")?.let { a ->
        (0 until a.length()).mapNotNull { i -> safeFile("images", a.optString(i))?.takeIf { it.exists() } }
    }.orEmpty()
    @Synchronized fun poster(key: String): File? = safeFile("posters", entry(key).optString("poster"))?.takeIf { it.exists() }
    private fun safeFile(kind: String, name: String): File? = if (validName.matches(name)) File(File(root, kind), name) else null
    @Synchronized private fun commit() {
        val previous = if (indexFile.baseFile.exists()) JSONObject(indexFile.readFully().toString(Charsets.UTF_8)) else JSONObject()
        try {
            val output = indexFile.startWrite()
            try { output.write(index.toString().toByteArray(Charsets.UTF_8)); indexFile.finishWrite(output) }
            catch (e: Exception) { indexFile.failWrite(output); throw e }
        } catch (e: Exception) { index = previous; throw e }
    }
    private fun copyImage(input: InputStream, kind: String): File {
        val dir = File(root, kind).apply { mkdirs() }
        val pending = File(dir, "${UUID.randomUUID()}.pending")
        try {
            pending.outputStream().use { out ->
                val buffer = ByteArray(32 * 1024); var total = 0L
                while (true) { val count = input.read(buffer); if (count < 0) break
                    total += count; require(total <= MAX_IMAGE) { "图片不能超过 20 MB" }; out.write(buffer, 0, count) }
            }
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(pending.path, options)
            require(options.outWidth > 0 && options.outHeight > 0 && options.outWidth.toLong() * options.outHeight <= 40_000_000) { "图片无效或尺寸过大" }
            val ext = when (options.outMimeType) { "image/jpeg" -> "jpg"; "image/png" -> "png"; "image/webp" -> "webp"; else -> error("仅支持 JPG、PNG、WebP") }
            val destination = File(dir, "${UUID.randomUUID()}.$ext")
            check(pending.renameTo(destination)) { "无法保存图片" }; return destination
        } finally { pending.delete() }
    }
    fun addPhotos(key: String, uris: List<Uri>) {
        require(uris.size + photos(key).size <= 20) { "每部电影最多 20 张剧照" }
        val added = mutableListOf<File>()
        try {
            for (uri in uris) context.contentResolver.openInputStream(uri)?.use { added += copyImage(it, "images") } ?: error("无法读取图片")
            synchronized(this) { val e = entry(key); val images = e.optJSONArray("images") ?: JSONArray()
                added.forEach { images.put(it.name) }; e.put("images", images); index.put(key, e); commit() }
        } catch (e: Exception) { added.forEach { it.delete() }; throw e }
    }
    fun setPoster(key: String, uri: Uri) {
        val file = context.contentResolver.openInputStream(uri)?.use { copyImage(it, "posters") } ?: error("无法读取海报")
        synchronized(this) { index.put(key, entry(key).put("poster", file.name)); commit() }
    }
    fun cachePoster(item: SeenItem) {
        val key = key(item)
        if (poster(key) != null || item.posterUrl.isBlank()) return
        val uri = Uri.parse(item.posterUrl)
        if (uri.scheme != "https" || uri.host !in setOf("m.media-amazon.com", "ia.media-imdb.com")) return
        client.newCall(Request.Builder().url(item.posterUrl).build()).execute().use { response ->
            if (!response.isSuccessful) return
            val file = response.body?.byteStream()?.use { copyImage(it, "posters") } ?: return
            synchronized(this) { if (poster(key) == null) { index.put(key, entry(key).put("poster", file.name)); commit() } else file.delete() }
        }
    }
    @Synchronized fun removePhoto(key: String, file: File): List<String> {
        val old = photos(key).map { it.name }
        index.put(key, entry(key).put("images", JSONArray(old.filterNot { it == file.name }))); commit(); return old
    }
    @Synchronized fun restorePhotos(key: String, names: List<String>) {
        index.put(key, entry(key).put("images", JSONArray(names.filter { safeFile("images", it)?.exists() == true }))); commit()
    }
    fun export(csv: String): File {
        val metadata: String
        val files: List<Pair<String, File>>
        synchronized(this) {
            metadata = index.toString()
            files = index.keys().asSequence().flatMap { key ->
                (photos(key).map { "images/${it.name}" to it } + listOfNotNull(poster(key)?.let { "posters/${it.name}" to it })).asSequence()
            }.distinctBy { it.first }.toList()
        }
        require(files.sumOf { it.second.length() } + metadata.toByteArray(Charsets.UTF_8).size + csv.toByteArray(Charsets.UTF_8).size + 1024 <= MAX_BACKUP) { "备份不能超过 512 MB，请先减少剧照" }
        val result = File(context.cacheDir, "seenema-backup.zip")
        val pending = File(context.cacheDir, "seenema-backup.pending")
        ZipOutputStream(pending.outputStream()).use { zip ->
            fun text(name: String, value: String) { zip.putNextEntry(ZipEntry(name)); zip.write(value.toByteArray(Charsets.UTF_8)); zip.closeEntry() }
            text("seenema.csv", csv); text("gallery.json", metadata); text("format.json", "{\"version\":1}")
            files.forEach { (name, file) -> zip.putNextEntry(ZipEntry(name)); file.inputStream().use { it.copyTo(zip) }; zip.closeEntry() }
        }
        check(pending.renameTo(result) || run { result.delete(); pending.renameTo(result) }) { "备份保存失败" }; return result
    }
    /** Unpack to a temporary folder, validate all paths before touching durable data. */
    fun importBackup(uri: Uri, accept: (String) -> Set<String>): String {
        val stage = File(context.cacheDir, "restore-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            var total = 0L; var count = 0
            context.contentResolver.openInputStream(uri)?.use { source -> ZipInputStream(source).use { zip ->
                while (true) { val e = zip.nextEntry ?: break; count++; require(count <= 10000) { "备份文件过多" }
                    if (e.isDirectory) { zip.closeEntry(); continue }
                    val parts = e.name.split('/')
                    val allowed = e.name in setOf("seenema.csv", "gallery.json", "format.json") || (parts.size == 2 && parts[0] in setOf("images", "posters") && validName.matches(parts[1]))
                    require(allowed) { "备份包含非法路径" }
                    val dest = File(stage, e.name); require(!dest.exists()) { "备份包含重复文件" }; dest.parentFile?.mkdirs()
                    var entrySize = 0L
                    dest.outputStream().use { out -> val buffer = ByteArray(32768)
                        while (true) { val n = zip.read(buffer); if (n < 0) break; total += n; entrySize += n
                            require(total <= MAX_BACKUP && entrySize <= MAX_IMAGE) { "备份过大" }; out.write(buffer, 0, n) } }
                    zip.closeEntry()
                }
            } } ?: error("无法读取备份")
            val csv = File(stage, "seenema.csv").readText()
            val metadata = JSONObject(File(stage, "gallery.json").readText())
            // Validate references before the caller merges the record list.
            for (key in metadata.keys()) {
                val e = metadata.getJSONObject(key); val images = e.optJSONArray("images") ?: JSONArray()
                require(images.length() <= 20) { "剧照数量超限" }
                val refs = (0 until images.length()).map { "images" to images.getString(it) } + listOfNotNull(e.optString("poster").takeIf { it.isNotBlank() }?.let { "posters" to it })
                refs.forEach { (kind, name) -> require(validName.matches(name) && File(stage, "$kind/$name").isFile) { "备份图片缺失" } }
            }
            val accepted = accept(csv)
            synchronized(this) {
                val incoming = mutableMapOf<String, JSONObject>()
                for (key in accepted) metadata.optJSONObject(key)?.let { e ->
                    val images = e.optJSONArray("images") ?: JSONArray()
                    val refs = (0 until images.length()).map { "images" to images.getString(it) } + listOfNotNull(e.optString("poster").takeIf { it.isNotBlank() }?.let { "posters" to it })
                    val imported = JSONObject().put("images", JSONArray())
                    refs.forEach { (kind, name) ->
                        val copied = File(stage,"$kind/$name").inputStream().use { copyImage(it,kind) }
                        if (kind == "posters") imported.put("poster",copied.name) else imported.getJSONArray("images").put(copied.name)
                    }
                    incoming[key] = imported
                }
                incoming.forEach { (key, value) -> index.put(key, value) }
                commit()
            }
            return csv
        } finally { stage.deleteRecursively() }
    }
}
