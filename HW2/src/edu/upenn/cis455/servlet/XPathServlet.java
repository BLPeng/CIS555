package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.*;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String xpath = request.getParameter("xpath");
		String xml = request.getParameter("xml");
		try {
			xpath = URLDecoder.decode(xpath, "utf-8");
			xml = URLDecoder.decode(xml, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		
		String[] xpaths = xpath.split("\r\n");
		XPathEngineImpl xpEngine = new XPathEngineImpl();
		
		//check validity of xpaths

		
		//match xml and xpaths


	
		
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
        writer.println("<h1> Full name: Xiaobin Chen </h1>");
        writer.println("<h2> SEAS login: xiaobinc </h2>");
        writer.println("<form action=\"/xpath\" method=\"post\">");
        writer.println("XPath");
        writer.println("<textarea rows=\"4\" cols=\"50\" name=\"xpath\"></textarea><br/>");
        writer.println("URL: <input type=\"text\" name=\"url\"><br/>");
        writer.println("<input type=\"submit\" value=\"submit\">");
        writer.println("</form>");
        writer.println("</body>");
        writer.println("</html>");
			
	}

}









