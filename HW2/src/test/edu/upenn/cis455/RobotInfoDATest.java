package test.edu.upenn.cis455;

import java.util.Date;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.RobotInfo;
import edu.upenn.cis455.storage.RobotInfoDA;


public class RobotInfoDATest extends TestCase {

	@BeforeClass
	public void setUp() {
		RobotInfoDA.init("testDatabase");
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
    }
}
