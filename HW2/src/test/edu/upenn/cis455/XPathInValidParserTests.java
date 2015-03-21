package test.edu.upenn.cis455;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.xpathengine.XPathParser;
import edu.upenn.cis455.xpathengine.XPathValidator;
import edu.upenn.cis455.xpathengine.XPathComponents.Step;

public class XPathInValidParserTests extends TestCase {
	private String content = "<bookstore><book category=\"COOKING\"><title lang=\"en\">Everyday Italian</title><author>Giada De Laurentiis</author><year>2005</year><price>30.00</price></book><book category=\"CHILDREN\"><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book><book category=\"WEB\"><title lang=\"en\">XQuery Kick Start</title><author>James McGovern</author><author>Per Bothner</author><author>Kurt Cagle</author><author>James Linn</author><author>Vaidyanathan Nagarajan</author><year>2003</year><price>49.99</price></book><book category=\"WEB\"><title lang=\"en\">Learning XML</title><author>Erik T. Ray</author><year>2003</year><price>39.95</price></book></bookstore>";
	private Document dom;
	private Tidy mTidy = new Tidy();
	private XPathParser parser;
	private XPathValidator validator;
	
	@Before
	public void setUp() throws Exception {
		mTidy.setXmlTags(true);
		mTidy.setInputEncoding("UTF-8");
		mTidy.setForceOutput(true);
		mTidy.setPrintBodyOnly(true);
		mTidy.setXmlOut(true);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
		dom = mTidy.parseDOM(inputStream, null);
		validator = new XPathValidator();
		parser = new XPathParser(dom, null);
	}

	/******** Test cases start **********/
	@Test
	public void testBadXPath1() {
		Step step = validator.getStep("/bookstore[book[title[text()==\"hahaha\"]]]");
		parser.setDocumentAndXPath(dom, step);
		assertTrue(parser.evaluate().size() == 0);
	}
	@Test
	public void testBadXPath2() {
		Step step = validator.getStep("/bookstore[book[title[@lang=\"ch\"]]]");
		parser.setDocumentAndXPath(dom, step);
		assertTrue(parser.evaluate().size() == 0);
	}
	@Test
	public void testBadXPath3() {
		Step step = validator.getStep("/bookstore/book[@category=\"MAGIC\"]/title[@lang=\"en\"]");
		parser.setDocumentAndXPath(dom, step);
		assertTrue(parser.evaluate().size() == 0);
	}
	@Test
	public void testBadXPath4() {
		Step step = validator.getStep("/bookstore/magazine[@category=\"CHILDREN\"]/title[text()=\"Harry Potter\"]");
		parser.setDocumentAndXPath(dom, step);
		assertTrue(parser.evaluate().size() == 0);
	}
	@Test
	public void testBadXPath5() {
		Step step = validator.getStep("/bookstore/book/TEXT");
		parser.setDocumentAndXPath(dom, step);
		assertTrue(parser.evaluate().size() == 0);
	}
}
