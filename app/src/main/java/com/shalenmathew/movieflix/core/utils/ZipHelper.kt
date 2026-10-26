package com.shalenmathew.movieflix.core.utils
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
object ZipHelper {
    private const val LIMIT=512L*1024*1024
    private fun allowed(name:String)=name=="backup.json" || (name.matches(Regex("[A-Za-z0-9_.-]+")) && listOf("poster_","banner_","gallery_").any{name.startsWith(it)})
    fun zip(files:List<File>,zipFile:File){
        require(files.sumOf{it.length()}<=LIMIT){"备份不能超过 512 MB"}
        ZipOutputStream(zipFile.outputStream().buffered()).use{zip->files.filter{it.isFile}.forEach{file->require(allowed(file.name));zip.putNextEntry(ZipEntry(file.name));file.inputStream().use{it.copyTo(zip)};zip.closeEntry()}}
    }
    fun unzip(zipFile:File,targetDirectory:File){
        var total=0L;var count=0;val names=mutableSetOf<String>()
        ZipInputStream(zipFile.inputStream().buffered()).use{zip->while(true){val entry=zip.nextEntry?:break
            require(!entry.isDirectory&&allowed(entry.name)&&names.add(entry.name)){"备份包含非法路径或重复文件"}
            require(++count<=10000){"备份文件过多"}
            val dest=File(targetDirectory,entry.name);require(dest.canonicalFile.parentFile==targetDirectory.canonicalFile)
            var size=0L;dest.outputStream().use{out->val buffer=ByteArray(32768);while(true){val n=zip.read(buffer);if(n<0)break;total+=n;size+=n;require(total<=LIMIT&&size<=20L*1024*1024){"备份过大"};out.write(buffer,0,n)}}
            zip.closeEntry()
        }}
    }
}
