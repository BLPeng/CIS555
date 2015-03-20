package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.*;

import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;



@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	private XPathEngineImpl xpathEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine(); 
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
			String[] xpaths = xpath.split(";");
			XPathEngineImpl xpEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
			xpEngine.setXPaths(xpaths);
			writer.println(getXPathValidities(xpaths, xpEngine));
		}
        writer.println("</body>");
        writer.println("</html>");
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

	private String getXPathValidities(String[] xpaths, XPathEngineImpl xpEngine) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		sb.append("<tr><th>XPath</th><th>IsValid</th></tr>");
		for (int i = 0; i < xpaths.length; i++) {
			sb.append("<tr><td>" + xpaths[i] + "</td><td>" + xpEngine.isValid(i) + "</td></tr>");
		}
		return sb.toString();
	}
	
	private String getXPathMatches(String[] xpaths, XPathEngineImpl xpEngine) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		sb.append("<tr><th>XPath</th><th>IsMatched</th></tr>");
		for (int i = 0; i < xpaths.length; i++) {
			sb.append("<tr><td>" + xpaths[i] + "</td><td>" + xpEngine.isValid(i) + "</td></tr>");
		}
		return sb.toString();
	}
}









