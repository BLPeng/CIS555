package test.edu.upenn.cis455;

import java.util.Date;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.upenn.cis455.storage.Content;
import edu.upenn.cis455.storage.ContentDA;


public class ContentDATest extends TestCase {

	@BeforeClass
	public void setUp() {
	    ContentDA.init("testDatabase");
	}
	
	@Test
	public void testPut() {
		String url = "http://www.google.com";
		String pageContent = "<book><s>sfasdfasdfasdf</s></book>";
		String type = "xml";
		Date date = new Date();
	    Content content = new Content(url, pageContent, date, type);
	    ContentDA.putEntry(content);
	    assertTrue(ContentDA.containsEntry(url));
	}
	
	@Test
	public void testGet() {
		String url = "http://www.google.com";
		System.out.println(ContentDA.containsEntry(url));
		String pageContent = "<book><s>sfasdfasdfasdf</s></book>";
		String type = "xml";
		Date date = new Date();
	    Content content = new Content(url, pageContent, date, type);
	    ContentDA.putEntry(content);
	    Content content1 = ContentDA.getEntry(url);
	    assertEquals(content.toString(), content1.toString());
	}

	@Test
	public void testDelete() {
		String url = "http://www.google1.com";
		String pageContent = "<book><s>sfasdfasdfasdf</s></book>";
		String type = "xml";
		Date date = new Date();
	    Content content = new Content(url, pageContent, date, type);
	    ContentDA.putEntry(content);
	    Content content1 = ContentDA.getEntry(url);
	    assertEquals(content.toString(), content1.toString());
	    ContentDA.deleteEntry(url);
	    content1 = ContentDA.getEntry(url);
	    assertNull(content1);
	}
	
	@AfterClass
    public void tearDown() {
		ContentDA.close();
    }
}
