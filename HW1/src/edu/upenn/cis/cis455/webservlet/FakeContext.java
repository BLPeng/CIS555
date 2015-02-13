package edu.upenn.cis.cis455.webservlet;
import javax.servlet.*;

import java.util.*;

/**
 * @author CIS455
 */
public class FakeContext implements ServletContext {
	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;

	public FakeContext() {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
	}
	/*
	 * Returns the servlet container attribute with the given name, 
	 * or null if there is no attribute by that name. An attribute allows
	 * a servlet container to give the servlet additional information not already 
	 * provided by this interface. See your server documentation for information about its attributes. 
	 * A list of supported attributes can be retrieved using getAttributeNames. 
	 */
	public Object getAttribute(String name) {			//OK
		return attributes.get(name);
	}
	
	public Enumeration getAttributeNames() {			//OK
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	//uripath - a String specifying the context path of another web application in the container. 
	public ServletContext getContext(String name) {		
		//TODO
		return null;
	}
	
	public String getInitParameter(String name) {		//OK	
		return initParams.get(name);
	}
	
	public Enumeration getInitParameterNames() {		//OK
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public int getMajorVersion() {		//ok
		return 2;
	}
	
	//file - a String specifying the name of a file
	//Returns the MIME type of the specified file, or null if the MIME type is not known.
	public String getMimeType(String file) {	//ignore
		return null;
	}
	
	public int getMinorVersion() {		//ok
		return 4;
	}
	
	public RequestDispatcher getNamedDispatcher(String name) {		//ignore
		return null;
	}
	
	public String getRealPath(String path) {	//??
		if (path == null || path.charAt(0) != '/')
			return null;
		String projPath = System.getProperty("user.dir");
		return projPath + path;
	}
	
	public RequestDispatcher getRequestDispatcher(String name) {	//ignore
		return null;
	}
	
	public java.net.URL getResource(String path) {		//ignore
		return null;
	}
	
	public java.io.InputStream getResourceAsStream(String path) {	//ignore
		return null;
	}
	
	public java.util.Set getResourcePaths(String path) {	// ignore http://tomcat.apache.org/tomcat-5.5-doc/servletapi/index.html
		return null;
	}
	
	public String getServerInfo() {				//OK
		return "Xiaobin's Java Server/1.0";
	}
	
	public Servlet getServlet(String name) {	//deprecated
		return null;
	}
	
	public String getServletContextName() {		//OK
		return "Xiaobin's Java Server";
	}
	
	public Enumeration getServletNames() {	//deprecated
		return null;
	}
	
	public Enumeration getServlets() {		//deprecated
		return null;
	}
	
	public void log(Exception exception, String msg) {		//deprecated
		log(msg, (Throwable) exception);
	}
	
	public void log(String msg) {		//ignore
		System.err.println(msg);
	}
	
	public void log(String message, Throwable throwable) {	//ignore
		System.err.println(message);
		throwable.printStackTrace(System.err);
	}
	
	public void removeAttribute(String name) {				//OK
		attributes.remove(name);
	}
	
	public void setAttribute(String name, Object object) {	//OK
		attributes.put(name, object);
	}
	
	public void setInitParam(String name, String value) {			//OK
		initParams.put(name, value);
	}
}
