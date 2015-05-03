package test.edu.upenn.cis455;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.RobotInfo;
import edu.upenn.cis455.storage.RobotInfoDA;
import edu.upenn.cis455.storage.UserDA;


public class RobotInfoDATest extends TestCase {
	private Environment env;
	@BeforeClass
	public void setUp() {
		String basedir = System.getProperty("user.dir");
		File file = new File(basedir, "testDatabase");
		boolean noExist = file.mkdirs();
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		env = new Environment(file, envConfig);
		RobotInfoDA.init(env);
	}
	
	@Test
	public void testPut() {
		String url = "http://www.google.com";
		String pageContent = "<book><s>sfasdfasdfasdf</s></book>";
		Date date = new Date();
		RobotInfo robotInfo = new RobotInfo(url, pageContent, date);
		RobotInfoDA.putEntry(robotInfo);
	    assertTrue(RobotInfoDA.containsEntry(url));
	}
	
	@Test
	public void testGet() {
		String url = "http://www.google.com";
//		System.out.println(RobotInfoDA.containsEntry(url));
		String pageContent = "<book><s>sfasdfasdfasdf</s></book>";
		Date date = new Date();
		RobotInfo robotInfo = new RobotInfo(url, pageContent, date);
		RobotInfoDA.putEntry(robotInfo);
	    assertTrue(RobotInfoDA.containsEntry(url));
	    RobotInfo robotInfo1 = RobotInfoDA.getEntry(url);
	    assertEquals(robotInfo.toString(), robotInfo1.toString());
	}

	@Test
	public void testDelete() {
		String url = "http://www.google1.com";
		String pageContent = "<book><s>sfasdfasdfasdf</s></book>";
		Date date = new Date();
		RobotInfo robotInfo = new RobotInfo(url, pageContent, date);
		RobotInfoDA.putEntry(robotInfo);
	    assertTrue(RobotInfoDA.containsEntry(url));
	    RobotInfo robotInfo1 = RobotInfoDA.getEntry(url);
	    assertEquals(robotInfo.toString(), robotInfo1.toString());
	    RobotInfoDA.deleteEntry(url);
	    robotInfo1 = RobotInfoDA.getEntry(url);
	    assertNull(robotInfo1);
	}
	
	@AfterClass
    public void tearDown() {
		RobotInfoDA.close();
		env.close();
    }
}
