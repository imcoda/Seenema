package com.shalenmathew.movieflix.diary
import com.shalenmathew.movieflix.core.utils.DiaryDate
import com.shalenmathew.movieflix.core.utils.ZipHelper
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry
class DiaryIntegrityTest {
    @Test fun validatesRealCalendarDates(){assertTrue(DiaryDate.valid("2024-02-29"));assertFalse(DiaryDate.valid("2025-02-29"));assertFalse(DiaryDate.valid("2026-13-01"));assertFalse(DiaryDate.valid("2026-1-01"))}
    @Test fun manualYearsRemainReadable(){assertEquals("1999",com.shalenmathew.movieflix.core.utils.formatDate("1999"));assertEquals("",com.shalenmathew.movieflix.core.utils.formatDate(null))}
    @Test fun groupsByWatchedMonth(){assertEquals("2026-10",DiaryDate.month("2026-10-26 12:00:00"));assertEquals("未注明日期",DiaryDate.month(null))}
    @Test fun backupRoundTripIncludesStills(){val dir=Files.createTempDirectory("diary").toFile();try{val input=File(dir,"backup.json").apply{writeText("{\"note\":\"记住的画面\"}")};val image=File(dir,"gallery_1.jpg").apply{writeBytes(byteArrayOf(1,2,3))};val zip=File(dir,"archive.zip");ZipHelper.zip(listOf(input,image),zip);val out=File(dir,"restored").apply{mkdirs()};ZipHelper.unzip(zip,out);assertEquals(input.readText(),File(out,"backup.json").readText());assertArrayEquals(image.readBytes(),File(out,"gallery_1.jpg").readBytes())}finally{dir.deleteRecursively()}}
    @Test fun rejectsZipTraversal(){val dir=Files.createTempDirectory("diary").toFile();try{val zip=File(dir,"archive.zip");ZipOutputStream(zip.outputStream()).use{it.putNextEntry(ZipEntry("../escaped"));it.write(1);it.closeEntry()};val out=File(dir,"restored").apply{mkdirs()};assertThrows(IllegalArgumentException::class.java){ZipHelper.unzip(zip,out)};assertFalse(File(dir,"escaped").exists())}finally{dir.deleteRecursively()}}
}
