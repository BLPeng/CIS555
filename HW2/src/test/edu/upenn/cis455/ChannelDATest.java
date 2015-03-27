package test.edu.upenn.cis455;

import java.util.Date;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.RobotInfoDA;
import edu.upenn.cis455.storage.UserDA;


public class ChannelDATest extends TestCase {
	@BeforeClass
	public void setUp() {
		ChannelDA.init("testDatabase");
	}
	
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
	
	@AfterClass
    public void tearDown() {
		ChannelDA.close();
    }

}
