package test.edu.upenn.cis455;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase 
{
  public static Test suite() 
  {
    try {
      Class[]  testClasses = {
        /* TODO: Add the names of your unit test classes here */
        // Class.forName("your.class.name.here") 
    		  XPathInValidTests.class,
    		  XPathValidatorTest.class,
    		  XPathParserTest.class,
    		  XPathInValidParserTests.class,
    		  UserDATest.class,
    		  ChannelDATest.class,
    		  ContentDATest.class,
    		  RobotInfoDATest.class,
    		  CrawlerWorkerTest.class
      };   
      
      return new TestSuite(testClasses);
    } catch(Exception e){
      e.printStackTrace();
    } 
    
    return null;
  }
}
