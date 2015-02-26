package edu.upenn.cis.cis455.test;

import edu.upenn.cis.cis455.webservletinterface.FakeSession;
import junit.framework.TestCase;

public class FakeSessionTest extends TestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	public void testSetMaxInactiveInterval() {
		FakeSession session = new FakeSession(null);
		session.setMaxInactiveInterval(100);
		assertEquals(session.getMaxInactiveInterval(), 100);
	}
	public void testSetAttribute() {
		FakeSession session = new FakeSession(null);
		session.setAttribute("a", 100);
		assertEquals(session.getAttribute("a"), 100);
	}
	public void testGetValue() {
		FakeSession session = new FakeSession(null);
		session.setAttribute("a", 100);
		assertEquals(session.getValue("a"), 100);
	}
	public void testPutValue() {
		FakeSession session = new FakeSession(null);
		session.putValue("a", 100);
		assertEquals(session.getValue("a"), 100);
	}
	

	public void testRemoveValue() {
		
		FakeSession session = new FakeSession(null);
		session.putValue("a", 100);
		session.removeValue("a");
	}
	
	public void testRemoveAttribute() {
		
		FakeSession session = new FakeSession(null);
		session.setAttribute("a", 100);
		session.removeAttribute("a");
	}
	
	public void testLastAccessTime() {
		FakeSession session = new FakeSession(null);
		session.setLastAccessTime(10000000);
		assertEquals(session.getLastAccessedTime(), 10000000);
	}
	
	public void testIsNew() {
		FakeSession session = new FakeSession(null);
		session.setIsNew(true);
		assertTrue(session.isNew());
	}
	//other methods are tested using LoginServlet
}
