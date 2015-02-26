package edu.upenn.cis.cis455.webservletinterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.upenn.cis.cis455.webserver.HttpRequestParser;
import edu.upenn.cis.cis455.webserver.HttpServer;
import edu.upenn.cis.cis455.webserver.HttpServerUtils;

/**
 * @author Todd J. Green
 */
public class FakeRequest implements HttpServletRequest {
	
	private String characterEncoding;
	private Socket socket;
	private String method;
	private HttpRequestParser requestParser;
	private HashMap<String, List<String>> headers;
	private HashMap<String, List<String>> m_params = new HashMap<String, List<String>>();
	private HashMap<String, Object> m_props = new HashMap<String, Object>();
	private FakeSession m_session = null;
	private List<Cookie> cookies;
	private Date date;
	private boolean fromCookie;
	
	public FakeRequest(Socket socket, HttpRequestParser requestParser) {
		init(socket, requestParser);
	}
	
	public FakeRequest(Socket socket, HttpRequestParser requestParser, FakeSession session) {
		init(socket, requestParser);
		m_session = session;
		fromCookie = false;
	}
	// for test
	public void setHeaders(HashMap<String, List<String>> headers) {
		this.headers = headers;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	
	private void init(Socket socket, HttpRequestParser requestParser) {
		if (socket == null || requestParser == null) return;
		this.socket = socket;
		this.requestParser = requestParser;
		characterEncoding = null;
		headers = requestParser.getHeaders();
		cookies = getCookiesFromHeaders(headers);
		method = requestParser.getMethod();
		date = new Date();
		if (cookies != null) {
			for (Cookie cookie: cookies) {
				if (cookie.getName().equalsIgnoreCase("JSESSIONID")) {
					m_session = (FakeSession) HttpServer.servletContainer.getSession(cookie.getValue());
					if (m_session != null){
						fromCookie = true;
						m_session.setIsNew(false);
						break;
					}
				}
			}
		}
		
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		return "BASIC_AUTH";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		if (cookies != null)
			return cookies.toArray(new Cookie[cookies.size()]);
		else 
			return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String header) {
		header = header.toLowerCase(Locale.ENGLISH);
		if (!headers.containsKey(header))		//1. not contains the header
			return -1;
		String dateStr = headers.get(header).get(0);		//first ele
		Date date = HttpServerUtils.convertDataFormat(dateStr, 0);
		if (date == null)	throw new IllegalArgumentException();		//not a valid string
		return date.getTime();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String header) {
		header = header.toLowerCase(Locale.ENGLISH);
		if (!headers.containsKey(header))
			return null;
		else {
			String value = headers.get(header).get(0);		//first ele
			return value;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration getHeaders(String header) {
		header = header.toLowerCase(Locale.ENGLISH);
		if (!headers.containsKey(header))
			return Collections.emptyEnumeration();
		else {
			Enumeration e = Collections.enumeration(headers.get(header));
			return e;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration getHeaderNames() {
		if (headers.size() == 0) {
			return Collections.emptyEnumeration();
		}else {
			return Collections.enumeration(headers.keySet());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String header) {
		header = header.toLowerCase(Locale.ENGLISH);
		if (!headers.containsKey(header)) {
			return -1;
		}
		String value = headers.get(header).get(0);
		int ret = Integer.valueOf(value);
		return ret;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return method;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		String reqUrl = requestParser.getUrl();
		String match = requestParser.matchUrlPattern(reqUrl);
		if (match == null)	return null;		//should not be null
		else {
			int sz = match.length();
			if (match.endsWith("/*")) {
				sz = sz - 2;
			} else if (match.endsWith("*")) {
				sz = sz - 1;
			}
			String ret = reqUrl.substring(sz);
			ret = ret.split("\\?")[0];
			if (ret.length() == 0 || ret.charAt(0) != '/')
				ret = "/" + ret;
			return ret;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {		//ignore
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {		//should return the HTTP GET query string, i.e., the portion after the “?” when a GET form is posted.
		if (!"GET".equalsIgnoreCase(requestParser.getMethod())) {
			return null;
		}
		String url = requestParser.getUrl();
		String[] parts = url.split("\\?");
		if (parts.length > 1)	return parts[1];
		else return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {		//ignore
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {	//ignore
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {		//ignore
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		for (Cookie cookie : cookies) {
			if("JSESSIONID".equalsIgnoreCase(cookie.getName())){
		       return cookie.getValue();
		    }
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {					//POST /some/path.html HTTP/1.1		/some/path.html 
		String reqUrl = requestParser.getUrl();
		String[] parts = reqUrl.split("\\?");
		return parts[0];
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		StringBuffer sb = new StringBuffer();
		sb.append(getScheme());
		sb.append("://");
		sb.append(getServerName());
		sb.append(":");
		sb.append(getServerPort());
		sb.append(getRequestURI());
		return sb;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		String ret = null;
		String matchPattern = requestParser.matchUrlPattern(requestParser.getUrl());
		if (matchPattern == null)	return ret;
		else if (matchPattern.endsWith("/*"))	return matchPattern.substring(0, matchPattern.length() - 2);		//???
		else return matchPattern;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean arg0) {
		if (arg0) {
			if (!hasSession()) {
				m_session = new FakeSession(HttpServer.servletContainer.getContext());
				HttpServer.servletContainer.addSession(m_session.getId(), m_session);
			}
		} else {
			if (! hasSession()) {
				m_session = null;
			}
		}
		if (m_session != null)
			m_session.setLastAccessTime(date.getTime());
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		return hasSession();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		return fromCookie;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {		//???
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		return Collections.enumeration(m_props.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		if (characterEncoding == null)
			return "ISO-8859-1";
		else
			return this.characterEncoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String characterEncoding)
			throws UnsupportedEncodingException {
		//check encoding
		String tmp = "test";
		String test = new String(tmp.getBytes(), characterEncoding);
		this.characterEncoding = characterEncoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		List<String> lengths = headers.get("Content-Length".toLowerCase(Locale.ENGLISH));
		if (lengths == null)	
			return -1;
		else {
			return Integer.valueOf(lengths.get(0));		//first element
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {		// ???
		return "application/octet-stream";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {		//ignore
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String arg0) {
		if (m_params.containsKey(arg0)) {
			return m_params.get(arg0).get(0);
		}else {
			return null;
		}
	}
		

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return Collections.enumeration(m_params.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String arg0) {
		if (m_params.containsKey(arg0)) {
			List<String> values = m_params.get(arg0);
			return values.toArray(new String[values.size()]);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		return m_params;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		return requestParser.getProtocol();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		return socket.getLocalAddress().getHostName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		return socket.getLocalPort();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	
	public BufferedReader getReader() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), getCharacterEncoding()));
		while (br.readLine() != null && br.readLine().length() != 0) {
				//Retrieves the body of the request 
		}
		return br;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		if (socket != null)
			return socket.getInetAddress().getHostAddress();
		else 
			return "0.0.0.0";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		if (socket != null)
			return socket.getInetAddress().getHostName();
		else 
			return socket.getInetAddress().getHostAddress();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {					//where is setLocale
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration getLocales() {			//ignore
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {		//ignore
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {		//deprecated
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		if (socket != null)
			return socket.getPort();
		else 
			return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		return socket.getLocalAddress().getHostName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		if (socket != null)
			return socket.getLocalAddress().getHostAddress();
		else 
			return "0.0.0.0";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		if (socket != null)
			return socket.getLocalPort();
		else 
			return -1; 
	}
	
	public void setParameter(String key, String value) {		//decode URL first
		if (m_params.containsKey(key)) {
			m_params.get(key).add(value);
		} else {
			 List<String> values = new ArrayList<String>();
			 values.add(value);
			 m_params.put(key, values);
		}
		
	}
	
	public void clearParameters() {
		m_params.clear();
	}
	
	public boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}
	
	private List<Cookie> getCookiesFromHeaders(HashMap<String, List<String>> headers) {
		if (headers != null) {
			List<String> values = headers.get("cookie");
			if (values != null) {
				List<Cookie> cookies = new ArrayList<Cookie>();
				for (String value : values) {			// multi-value for header
					String[] cookiess = value.split("[;,]");		//A server should also accept comma (,) as the separator between cookie-values for future compatibility.
					for (String v : cookiess) {			// multi-cookie value
						String[] pair = v.trim().split("=");
						if (pair.length != 2)	continue;	// invalid value
						Cookie cookie = new Cookie(pair[0], pair[1]);
						cookies.add(cookie);
					}
				}
				return cookies;
			}
		}
		return null;
	}
		
}
