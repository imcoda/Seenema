package sitavi.seenema
import org.junit.Assert.*
import org.junit.Test
class SeenCsvTest {
    private fun record()=SeenItem("tt0133093","movie","黑客帝国, The Matrix","1999","https://m.media-amazon.com/poster.jpg",Status.SEEN,0,"第一行\n第二行, \"台词\"","2026-10-26T12:00:00")
    @Test fun unicodeQuotesAndLineBreaksRoundTrip(){val item=record();assertEquals(listOf(item),SeenCsv.decode(SeenCsv.encode(listOf(item))))}
    @Test fun unratedIsNotRemovedOrStoredAsZero(){val text=SeenCsv.encode(listOf(record()));assertFalse(text.contains(",seen,0,"));assertEquals(0,SeenCsv.decode(text).single().rating)}
    @Test fun importsLegacySevenColumns(){val items=SeenCsv.decode("id,type,title,year,poster_url,rating,added_at\n tt1,movie,Title,2000,,4,2020-01-01T12:00:00".replace(" tt1","tt1"));assertEquals(4,items.single().rating);assertEquals("",items.single().note)}
    @Test fun importsLegacySixColumns(){val item=SeenCsv.decode("id,type,title,year,poster_url,added_at\ntt1,movie,Title,2000,,2020-01-01T12:00:00").single();assertEquals(0,item.rating);assertEquals(Status.SEEN,item.status)}
}
