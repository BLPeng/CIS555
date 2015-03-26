package test.edu.upenn.cis455;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.xpathengine.XPathComponents.Step;
import edu.upenn.cis455.xpathengine.XPathValidator;

public class XPathValidatorTest extends TestCase {
	public XPathValidator validator;
	@Before
	public void setUp() throws Exception {
		validator = new XPathValidator();
	}

	/*
	 * 
	 * "/catalog/cd[@title=\"Empire Burlesque\"][@artist=\"Bob Dylan\"]",
	   "/catalog/cd[@title=\"Empire Burlesque\"]",
	   "/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]",
	   "/catalog/cd[@title=\"Empire Burlesque\"]",
	   "/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]",
	   "/catalog/badelem",
	 */
	/******** Test cases start **********/
	@Test
	public void testValidXPath1() {
		Step step = validator.getStep("/catalog/badelem");
		assertNotNull(step);
	}
	@Test
	public void testValidXPath2() {
		Step step = validator.getStep("/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]");
		assertNotNull(step);
	}
	@Test
	public void testValidXPath3() {
		Step step = validator.getStep("/catalog/cd[@title=\"Empire Burlesque\"]");
		assertNotNull(step);
	}
	@Test
	public void testValidXPath4() {
		Step step = validator.getStep("/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]");
		assertNotNull(step);
	}
	@Test
	public void testValidXPath5() {
		Step step = validator.getStep("/catalog/cd[@title=\"Empire Burlesque\"]");
		assertNotNull(step);
	}
	@Test
	public void testValidXPath6() {
		Step step = validator.getStep("/catalog/cd[@title=\"Empire Burlesque\"][@artist=\"Bob Dylan\"]");
		assertNotNull(step);
	}
	

}
