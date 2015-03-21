package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;



@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	private XPathEngineImpl xpathEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine(); 
	private Tidy mTidy = new Tidy();
	private Document document;
	/* You may want to override one or both of the following methods */
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String xpath = request.getParameter("xpaths");
		String url = request.getParameter("url");
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			xpath = URLDecoder.decode(xpath, "utf-8").trim();
			url = URLDecoder.decode(url, "utf-8").trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>XPath Result</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<h2> Xiaobin Chen </h2>");
        writer.println("<h2> SEAS: xiaobinc </h2>");
        writer.println("<h3> XPath Result: </h3>");
		if (xpath.isEmpty()) {
			writer.println("Error: Empty xpath <br/>");
		} else if (url.isEmpty()) {
			writer.println("Error: Empty URL <br/>");
		} else {
			if (!url.toLowerCase().startsWith("http://")) {
				url = "http://" + url;
			}
			String[] xpaths = xpath.split(";");
			XPathEngineImpl xpEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
			xpEngine.setXPaths(xpaths);
			writer.println(getXPathValidities(xpaths, xpEngine));
			writer.println(url);
			try {
				writer.println(getXPathMatches(xpaths, getDOMFromURL(url), xpEngine));
			} catch (UnsupportedEncodingException e) {
				writer.println("Failed to fetch page");
				e.printStackTrace();
			}
		}
        writer.println("</body>");
        writer.println("</html>");
        writer.close();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		//generate form
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Welcome XPath</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<h2> Xiaobin Chen </h2>");
        writer.println("<h2> SEAS: xiaobinc </h2>");
        writer.println("<form method=\"post\">");
        writer.println("XPaths: separate by semicolons<br/>");
        writer.println("<input type=\"text\" name=\"xpaths\" size=\"100\" ><br/>");
        writer.println("URL:<br/>");
        writer.println("<input type=\"text\" name=\"url\" size=\"100\"><br/>");
        writer.println("<input type=\"submit\" value=\"submit\">");
        writer.println("</form>");
        writer.println("</body>");
        writer.println("</html>");
			
	}
	//show xpath validity result
	private String getXPathValidities(String[] xpaths, XPathEngineImpl xpEngine) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		sb.append("<tr><th>XPath</th><th>IsValid</th></tr>");
		for (int i = 0; i < xpaths.length; i++) {
			sb.append("<tr><td>" + xpaths[i] + "</td><td>" + xpEngine.isValid(i) + "</td></tr>");
		}
		sb.append("</table><br/><br/>");
		return sb.toString();
	}
	
	//show xpath match result
	private String getXPathMatches(String[] xpaths, Document document, XPathEngineImpl xpEngine) {
		xpEngine.setXPaths(xpaths);
		boolean[] ret = xpEngine.evaluate(document);
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		sb.append("<tr><th>XPath</th><th>IsMatched</th></tr>");
		for (int i = 0; i < xpaths.length; i++) {
			sb.append("<tr><td>" + xpaths[i] + "</td><td>" + ret[i] + "</td></tr>");
		}
		return sb.toString();
	}
	
	
	
	private Document getDOMFromURL(String URL) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		boolean isXML = false;
		try {
			// http client
			URL myURL = new URL(URL);
			Socket socket = new Socket(myURL.getHost(), 80);
			OutputStream theOutput = socket.getOutputStream();
	        PrintWriter out = new PrintWriter(theOutput, false);
	        String req = "GET " + myURL + " HTTP/1.0\r\n";
	        out.print(req); 
	        out.print("Host:" + myURL.getHost() + "\r\n");
	        out.print("Accept: application/xml, text/html, text/html, application/rss+xml\r\n");
			out.print("\r\n"); 
			out.flush(); 
			BufferedReader in = new BufferedReader(
			      new InputStreamReader(socket.getInputStream()));
			String line;
			boolean isHead = true;
			
			while ((line = in.readLine()) != null) {
				if (isHead) {
					if (line.contains("xml")) {				//check header
						isXML = true;						
					}
					if (line.isEmpty()) {
						isHead = false;
					}
				} else {
					sb.append(line);
					sb.append(System.lineSeparator());
				}
			}
			socket.close();
		}catch(Exception e) {
			return null;
		}
		if (isXML) {
			mTidy.setXmlTags(true);
		}
		String page = sb.toString();
		mTidy.setInputEncoding("UTF-8");
		mTidy.setForceOutput(true);
		mTidy.setPrintBodyOnly(true);
		mTidy.setXmlOut(true);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(page.getBytes("UTF-8"));
		document = mTidy.parseDOM(inputStream, null);

		return document;
	}
	
}









