package ink.c0d4.seenema.core.utils
import android.content.Context
import android.net.Uri
import android.graphics.BitmapFactory
import java.io.File
import java.io.InputStream
import java.util.UUID
object DiaryImages {
    fun copy(context:Context,uri:Uri,prefix:String):File=context.contentResolver.openInputStream(uri)?.use{copyStream(context,it,prefix)}?:error("无法读取图片")
    fun copyStream(context:Context,input:InputStream,prefix:String):File {
        require(prefix in setOf("gallery_","poster_","banner_"))
        val file=File(context.filesDir,prefix+UUID.randomUUID()+".jpg")
        try{
            var total=0L;file.outputStream().use{out->val buffer=ByteArray(32768);while(true){val n=input.read(buffer);if(n<0)break;total+=n;require(total<=20L*1024*1024){"单张图片不能超过 20 MB"};out.write(buffer,0,n)}}
            val info=BitmapFactory.Options().apply{inJustDecodeBounds=true};BitmapFactory.decodeFile(file.path,info)
            require(info.outWidth>0&&info.outHeight>0&&info.outWidth.toLong()*info.outHeight<=40_000_000&&info.outMimeType in setOf("image/jpeg","image/png","image/webp")){"请选择有效的 JPG、PNG 或 WebP 图片"}
            return file
        }catch(e:Exception){file.delete();throw e}
    }
}
