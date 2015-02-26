package edu.upenn.cis.cis455.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import edu.upenn.cis.cis455.webservletinterface.FakeRequest;
import edu.upenn.cis.cis455.webservletinterface.FakeResponse;
import junit.framework.TestCase;

public class FakeResponseTest extends TestCase {
	
	FakeRequest req;
	protected void setUp() throws Exception {
		super.setUp();
		req = new FakeRequest(null, null);
		HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		List<String> list = new ArrayList<>();
		list.add("Thu, 26 Feb 2015 00:01:11 GMT");
		headers.put("date", list);
		list = new ArrayList<>();
		list.add("1000");
		headers.put("content-length", list);
		req.setHeaders(headers);
	}
	
	public void testContainsHeader() throws IOException {
		FakeResponse res = new FakeResponse(null, req);
		res.setHeader("date", "Thu, 26 Feb 2015 00:01:11 GMT");
		assertTrue(res.containsHeader("date"));
	}
	
	public void testSetHeader() throws IOException {
		FakeResponse res = new FakeResponse(null, req);
		res.setHeader("date", "Thu, 26 Feb 2015 00:01:11 GMT");
		assertTrue(res.containsHeader("date"));
	}
	
	public void testAddHeader() throws IOException {
		FakeResponse res = new FakeResponse(null, req);
		res.addHeader("date", "Thu, 26 Feb 2015 00:01:11 GMT");
		res.addHeader("content-length", "1000");
		assertTrue(res.containsHeader("date"));
		assertTrue(res.containsHeader("content-length"));
	}
	
	public void testGetCharacterEncoding() throws IOException {
		FakeResponse res = new FakeResponse(null, req);
		assertEquals(res.getCharacterEncoding(), "ISO-8859-1");
	}
	
	public void testGetContentType() throws IOException {
		FakeResponse res = new FakeResponse(null, req);
		assertEquals(res.getContentType(), "text/html");
	}
	
	public void testSetContentType() throws IOException {
		FakeResponse res = new FakeResponse(null, req);
		res.setContentType("text/plain");
		assertEquals(res.getContentType(), "text/plain");
	}
	
	public void testSetLocale() throws IOException {
		FakeResponse res = new FakeResponse(null, req);
		res.setLocale(Locale.ENGLISH);;
		assertEquals(res.getLocale(), Locale.ENGLISH);
	}
	
	//other methods are tested using servlets
}
