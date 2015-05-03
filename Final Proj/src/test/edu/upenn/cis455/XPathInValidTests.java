package test.edu.upenn.cis455;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.xpathengine.XPathValidator;
import edu.upenn.cis455.xpathengine.XPathComponents.Step;

public class XPathInValidTests extends TestCase {
	public XPathValidator validator;
	@Before
	public void setUp() throws Exception {
		validator = new XPathValidator();
	}

	@Test
	public void testinValidXPath1() {
		Step step = validator.getStep("/catalog/cd[title=\"Empire Burlesque\"][@artist=\"Bob Dylan\"]");
		assertNull(step);
	}
	@Test
	public void testinValidXPath2() {
		Step step = validator.getStep("catalog/cd[title=\"Empire Burlesque\"][@artist=\"Bob Dylan\"]");
		assertNull(step);
	}
	@Test
	public void testinValidXPath3() {
		Step step = validator.getStep("/[catalog/cd[title=\"Empire Burlesque\"][@artist=\"Bob Dylan\"]");
		assertNull(step);
	}
	@Test
	public void testinValidXPath4() {
		Step step = validator.getStep("/\"catalog/cd[title=\"Empire Burlesque\"][@artist=\"Bob Dylan\"]");
		assertNull(step);
	}
	@Test
	public void testinValidXPath5() {
		Step step = validator.getStep("/catalog/cd[title=Empire Burlesque\"][@artist=\"Bob Dylan\"]");
		assertNull(step);
	}
	@Test
	public void testQuoteEscaping6() {
		Step step = validator.getStep("/catalog/cd[title=\"Empire Burlesque\"dd ][@artist=\"Bob Dylan\"]");
		assertNull(step);
	}


}
