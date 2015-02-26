package edu.upenn.cis.cis455.test;

import edu.upenn.cis.cis455.webservletinterface.FakeContext;
import junit.framework.TestCase;

public class FakeContextTest extends TestCase {
	FakeContext context = new FakeContext();
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGetAttribute() {
		assertEquals(context.getAttribute("a"), null);
		context.setAttribute("a", "aaa");
		assertEquals(context.getAttribute("a"), "aaa");
	}
	
	public void testGetInitParameter() {
		context.setInitParam("a", "100");
		assertEquals(context.getInitParameter("a"), "100");
	}
	
	public void testGetMajorVersion() {
		assertEquals(context.getMajorVersion(), 2);
	}
	
	public void testGetMinorVersion() {
		assertEquals(context.getMinorVersion(), 4);
	}
	
	public void testGetMimeType() {
		assertEquals(context.getMimeType(null), null);
	}
	
	public void testRemoveAttribute() {
		context.setAttribute("a", "aaa");
		assertEquals(context.getAttribute("a"), "aaa");
		context.removeAttribute("a");
		assertEquals(context.getAttribute("a"), null);
	}

}
