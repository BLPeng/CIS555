package edu.upenn.cis.cis455.webservletinterface;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import edu.upenn.cis.cis455.webserver.HttpServer;

/**
 * @author Todd J. Green
 */
public class FakeSession implements HttpSession {
	private Properties m_props = new Properties();
	private ServletContext servletContext;
	private int maxInactiveInterval;
	private boolean isNew = true;
	private boolean m_valid = true;
	private Date date;
	private long creationTime;
	private long lastAccessTime;
	private UUID UID;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 */
	
	public FakeSession(ServletContext sc) {
		date = new Date();
		servletContext = sc;
		creationTime = date.getTime();
		lastAccessTime = creationTime;
		m_valid = true;
		isNew = true;
		maxInactiveInterval = HttpServer.servletContainer.getSesstionTimeout();
		UID = UUID.randomUUID();
	}
	
	public long getCreationTime() {
		if (m_valid)	return this.creationTime;
		else	throw new IllegalStateException(); 
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	public String getId() {
		if (m_valid){
			return UID.toString();
		}
		else {
			throw new IllegalStateException(); 
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		return lastAccessTime;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int mii) {
		// Specifies the time, in seconds
		maxInactiveInterval = mii;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	public HttpSessionContext getSessionContext() {		//deprecated
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		if (m_valid){
			return m_props.get(name);
		}
		else {
			throw new IllegalStateException(); 
		}	
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	public Object getValue(String arg0) {		//deprecated
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		if (m_valid){
			return m_props.keys();
		}
		else {
			throw new IllegalStateException(); 
		}			
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */
	public String[] getValueNames() {		//deprecated
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		if (!m_valid)	throw new IllegalStateException();
		if (value == null) {
			removeAttribute(name);
		}
		m_props.put(name, value);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String arg0, Object arg1) {	//deprecated
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		if (!m_valid)	throw new IllegalStateException();
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	public void removeValue(String arg0) {		//deprecated
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	public void invalidate() {
		HttpServer.servletContainer.removeSession(UID.toString());
		m_props.clear();
		m_valid = false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	public boolean isNew() {
		return isNew;
	}

	boolean isValid() {
		return m_valid;
	}
	
	public void setLastAccessTime(long time) {
		this.lastAccessTime = time;
	}
	
	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}
	
}
