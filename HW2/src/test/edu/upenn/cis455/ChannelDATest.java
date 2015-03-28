package test.edu.upenn.cis455;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.RobotInfoDA;
import edu.upenn.cis455.storage.UserDA;


public class ChannelDATest extends TestCase {
	@BeforeClass
	public void setUp() {
		String basedir = System.getProperty("user.dir");
		File file = new File(basedir, "testDatabase");
		boolean noExist = file.mkdirs();
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		Environment env = new Environment(file, envConfig);
		ChannelDA.init(env);
	}
	
	@Test
	public void testPut() {
		String username = "xb";
		String name = "1";
		String[] xpaths = {"/book/s", "test/test1[test() = \"1\"]"};
		String url = "http://www.google.com";
		Date date = new Date();
	    Channel channel = new Channel(name, username, url, date, xpaths);
	    ChannelDA.putEntry(channel);
	    Channel channel1 = ChannelDA.getEntry("1");
	    assertEquals(channel.toString(), channel1.toString());
	}
	
	@Test
	public void testGet() {
		String username = "xb";
		String name = "1";
		String[] xpaths = {"/book/s", "test/test1[test() = \"1\"]"};
		String url = "http://www.google.com";
		Date date = new Date();
	    Channel channel = new Channel(name, username, url, date, xpaths);
	    ChannelDA.putEntry(channel);
	    Channel channel1 = ChannelDA.getEntry("1");
	    assertEquals(channel.toString(), channel1.toString());
	}

	@Test
	public void testDelete() {
		String username = "aa";
		String name = "1";
		String[] xpaths = {"/book/s", "test/test1[test() = \"1\"]"};
		String url = "http://www.google.com";
		Date date = new Date();
	    Channel channel = new Channel(name, username, url, date, xpaths);
	    ChannelDA.putEntry(channel);
	    Channel channel1 = ChannelDA.getEntry("1");
	    assertEquals(channel.toString(), channel1.toString());
	    ChannelDA.deleteEntry("1");
	    channel1 = ChannelDA.getEntry("1");
	    assertNull(channel1);
	}
	
	@AfterClass
    public void tearDown() {
		ChannelDA.close();
    }

}
