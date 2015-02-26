package edu.upenn.cis.cis455.test;

import edu.upenn.cis.cis455.webservletinterface.FakeConfig;
import junit.framework.TestCase;

public class FakeConfigTest extends TestCase {
	FakeConfig config;
	protected void setUp() throws Exception {
		super.setUp();
		config = new FakeConfig("s", null);
		config.setInitParam("a", "100");
		config.setInitParam("b", "200");
	}
	public void testGetServletContext() {
		assertEquals(config.getServletContext(), null);
	}
	public void testGetServletName() {
		assertEquals(config.getServletName(), "s");
	}
	public void testGetInitParameter() {
		assertEquals(config.getInitParameter("a"), "100");
		assertEquals(config.getInitParameter("b"), "200");
	}
}
