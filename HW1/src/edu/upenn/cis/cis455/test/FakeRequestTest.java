package edu.upenn.cis.cis455.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.upenn.cis.cis455.webservletinterface.FakeRequest;
import junit.framework.TestCase;

public class FakeRequestTest extends TestCase {
	
	public void testGetAuthType() {
		FakeRequest req = new FakeRequest(null, null);
		assertEquals(req.getAuthType(), "BASIC_AUTH");
	}
	public void testGetScheme() {
		FakeRequest req = new FakeRequest(null, null);
		assertEquals(req.getScheme(), "http");
	}
	public void testGetDateHeader() {
		FakeRequest req = new FakeRequest(null, null);
		HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		List<String> list = new ArrayList<>();
		list.add("Thu, 26 Feb 2015 00:01:11 GMT");
		headers.put("date", list);
		req.setHeaders(headers);
		assertEquals(String.valueOf(req.getDateHeader("date")), "1424908871000");
	}
	
	public void testGetHeader() {
		FakeRequest req = new FakeRequest(null, null);
		HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		List<String> list = new ArrayList<>();
		list.add("Thu, 26 Feb 2015 00:01:11 GMT");
		headers.put("date", list);
		req.setHeaders(headers);
		assertEquals(req.getHeader("date"), "Thu, 26 Feb 2015 00:01:11 GMT");
	}
	
	public void testGetIntHeader() {
		FakeRequest req = new FakeRequest(null, null);
		HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		List<String> list = new ArrayList<>();
		list.add("1000");
		headers.put("content-length", list);
		req.setHeaders(headers);
		assertEquals(req.getIntHeader("content-length"), 1000);
	}
	public void testGetMethod() {
		FakeRequest req = new FakeRequest(null, null);
		req.setMethod("GET");
		assertEquals(req.getMethod(), "GET");
	}
	public void testGetCharacterEncoding() {
		FakeRequest req = new FakeRequest(null, null);
		assertEquals(req.getCharacterEncoding(), "ISO-8859-1");
	}
	public void testGetContentLength() {
		FakeRequest req = new FakeRequest(null, null);
		HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		List<String> list = new ArrayList<>();
		list.add("1000");
		headers.put("content-length", list);
		req.setHeaders(headers);
		assertEquals(req.getContentLength(), 1000);
	}
	public void testGetContentType() {
		FakeRequest req = new FakeRequest(null, null);
		assertEquals(req.getContentType(), "application/octet-stream");
	}
	//other methods are tested using TestServlet.class
}
