package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.util.Date;
import junit.framework.TestCase;
import org.junit.Test;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDA;


public class ChannelDATest extends TestCase {

	
	@Test
	public void testPut() {
		String username = "xb";
		String[] xpaths = {"/book/s", "test/test1[test() = \"1\"]"};
		String url = "http://www.google.com";
		Date date = new Date();
	    Channel channel = new Channel(username, url, date, xpaths);
	    ChannelDA.putEntry(channel);
	    Channel channel1 = ChannelDA.getEntry("xb");
	    assertEquals(channel.toString(), channel1.toString());
	}
	
	@Test
	public void testGet() {
		String username = "xb";
		String[] xpaths = {"/book/s", "test/test1[test() = \"1\"]"};
		String url = "http://www.google.com";
		Date date = new Date();
	    Channel channel = new Channel(username, url, date, xpaths);
	    ChannelDA.putEntry(channel);
	    Channel channel1 = ChannelDA.getEntry("xb");
	    assertEquals(channel.toString(), channel1.toString());
	}

	@Test
	public void testDelete() {
		String username = "aa";
		String[] xpaths = {"/book/s", "test/test1[test() = \"1\"]"};
		String url = "http://www.google.com";
		Date date = new Date();
	    Channel channel = new Channel(username, url, date, xpaths);
	    ChannelDA.putEntry(channel);
	    Channel channel1 = ChannelDA.getEntry("aa");
	    assertEquals(channel.toString(), channel1.toString());
	    ChannelDA.deleteEntry("aa");
	    channel1 = ChannelDA.getEntry("aa");
	    assertNull(channel1);
	}
}
