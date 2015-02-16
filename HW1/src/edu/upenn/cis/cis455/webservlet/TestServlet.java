package edu.upenn.cis.cis455.webservlet;



import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
  {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html><head><title>Test</title></head><body>");
    out.println("RequestURL: ["+request.getRequestURL()+"]<br>");
    out.println("RequestURI: ["+request.getRequestURI()+"]<br>");
    out.println("PathInfo: ["+request.getPathInfo()+"]<br>");
    out.println("Context path: ["+request.getContextPath()+"]<br>");
    out.println("Accept-Encoding: ["+request.getHeader("Accept-Encoding")+"]<br>");
    out.println("Method: ["+request.getMethod()+"]<br>");
    out.println("URL: [" + request.getRequestURL()+"]<br>");
	out.println("URI: [" + request.getRequestURI()+"]<br>");
	out.println("Scheme: [" + request.getScheme()+"]<br>");
	out.println("Server Name: [" + request.getServerName()+"]<br>");
	out.println("Port: [" + request.getLocalPort()+"]<br>");
	out.println("Context Path: [" + request.getContextPath()+"]<br>");
	out.println("Servlet Path: [" + request.getServletPath()+"]<br>");
	out.println("Path Info: [" + request.getPathInfo()+"]<br>");
	out.println("Query: [" + request.getQueryString()+"]<br>");
	out.println("Local Name: [" + request.getLocalName()+"]<br>");
	out.println("Local Addr: [" + request.getLocalAddr()+"]<br>");
	out.println("Remote Port: [" + request.getRemotePort()+"]<br>");
	out.println("Remote Host: [" + request.getRemoteHost()+"]<br>");
	out.println("Remote Addr: [" + request.getRemoteAddr()+"]<br>");
	out.println("</body></html>");
  }
}
  
