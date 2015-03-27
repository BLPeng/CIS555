package test.edu.upenn.cis455;

import java.util.Date;

import junit.framework.TestCase;
import org.junit.Test;
import edu.upenn.cis455.storage.RobotInfo;
import edu.upenn.cis455.storage.RobotInfoDA;


public class RobotInfoDATest extends TestCase {

	
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
		String url = "http://www.google.com";
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
}
