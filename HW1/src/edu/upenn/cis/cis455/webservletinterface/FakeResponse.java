package edu.upenn.cis.cis455.webservletinterface;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

//TODO cookies 


import javax.servlet.http.HttpSession;

import edu.upenn.cis.cis455.webserver.HttpServerUtils;

/**
 * @author tjgreen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FakeResponse implements HttpServletResponse {
	private Locale locale;
	private Socket socket;
	private FakeRequest servletRequest;
	private int statusCode = 200;
	private int bufferSize;
	private StringBuffer sb;
	private boolean isCommitted = false;
	private boolean isHeadersSent = false;
	private PrintWriter writer;
	private List<Cookie> cookies = new ArrayList<Cookie>();
	private HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
	private SimpleDateFormat format = new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss z");
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	
	public FakeResponse(Socket socket, FakeRequest servletRequest) throws IOException {
		this.servletRequest = servletRequest;
		sb = new StringBuffer();
		this.socket = socket;
		Cookie[] cookie = servletRequest.getCookies();
		if (cookie != null)
			for (int i = 0; i < cookie.length; i++)
				cookies.add(cookie[i]);
	}
	public void addCookie(Cookie cookie) {
		if (cookie != null){
			cookies.add(cookie);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String header) {
		return headers.containsKey(header);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {		//deprecated
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {		//deprecated
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {				//deprecated
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {		//deprecated
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int code, String msg) throws IOException {
		if (isCommitted) {
			throw new IllegalStateException();
		} else {
			setStatus(code);
			PrintWriter pw = getWriter();
			StringBuilder sb = new StringBuilder();
			sb.append(HttpServerUtils.genHTTPContent(msg));
			pw.write(sb.toString());
		}
		isCommitted = true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int code) throws IOException {
		if (isCommitted) {
			throw new IllegalStateException();
		} else {
			sb.setLength(0);
			setStatus(code);
			PrintWriter pw = getWriter();
			pw.flush();
		}
		isCommitted = true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String url) throws IOException {
		statusCode = 302;
		this.setHeader("Location", url);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String header, long value) {
		header = header.toLowerCase(Locale.ENGLISH);
		List<String> values = new ArrayList<String>();
		values.add(format.format(new Date(value)));
		headers.put(header, values);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String header, long value) {
		header = header.toLowerCase(Locale.ENGLISH);
		List<String> values = headers.get(header);
		if (values == null) {
			values = new ArrayList<String>();
			values.add(format.format(new Date(value)));
			headers.put(header, values);
		} else {
			headers.get(header).add(format.format(new Date(value)));
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String header, String value) {
		header = header.toLowerCase(Locale.ENGLISH);
		List<String> values = new ArrayList<String>();
		values.add(value);
		headers.put(header, values);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String header, String value) {
		header = header.toLowerCase(Locale.ENGLISH);
		List<String> values = headers.get(header);
		if (values == null) {
			values = new ArrayList<String>();
			values.add(value);
			headers.put(header, values);
		} else {
			headers.get(header).add(value);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String header, int value) {
		header = header.toLowerCase(Locale.ENGLISH);
		List<String> values = new ArrayList<String>();
		values.add(String.valueOf(value));
		headers.put(header, values);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String header, int value) {
		header = header.toLowerCase(Locale.ENGLISH);
		List<String> values = headers.get(header);
		if (values == null) {
			values = new ArrayList<String>();
			values.add(String.valueOf(value));
			headers.put(header, values);
		} else {
			headers.get(header).add(String.valueOf(value));
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int statusCode) {
		this.statusCode = statusCode;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int statusCode, String arg1) {		//deprecated
		this.statusCode = statusCode;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return "ISO-8859-1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		List<String> values = headers.get("content-type");
		if (values != null)
			return values.get(0);
		else
			return "text/html";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {		//ignore
		return null;
	}
	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		if (writer == null) {
			writer = new ResponsePrintWriter(new OutputStreamWriter(
				    socket.getOutputStream(), getCharacterEncoding()), true);
		}
		return writer; 
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String encoding) {
		final String header = "content-type";
		List<String> values = headers.get(header);
		if (values == null) {
			values = new ArrayList<String>();
			values.add(String.valueOf(encoding));
			headers.put(header, values);
		} else {
			headers.get(header).add(String.valueOf(encoding));
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int length) {
		final String header = "content-length";
		List<String> values = headers.get(header);
		if (values == null) {
			values = new ArrayList<String>();
			values.add(String.valueOf(length));
			headers.put(header, values);
		} else {
			headers.get(header).add(String.valueOf(length));
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String contentType) {
		final String header = "content-type";
		List<String> values = headers.get(header);
		if (values == null) {
			values = new ArrayList<String>();
			values.add(contentType);
			headers.put(header, values);
		} else {
			headers.get(header).set(0, contentType);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int size) {
		if (isCommitted) {
			throw new IllegalStateException();
		}
		this.bufferSize = size;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		return this.bufferSize;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		if (writer == null) {
			writer = getWriter();
		}
		writer.flush();
		isCommitted = true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		if (isCommitted) {
			throw new  IllegalStateException();
		} else {
			sb.setLength(0);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		return isCommitted;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		if (isCommitted) {
			throw new IllegalStateException("Already committed");
		}
		cookies = new ArrayList<Cookie>();
		headers = new HashMap<String,List<String>>();
		locale = null;
		statusCode = 200;
		bufferSize = 0;
		isCommitted = false;
		isHeadersSent = false;
		sb.setLength(0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		return this.locale;
	}
	
	private String resInitLine() {
		StringBuilder sb = new StringBuilder();
		sb.append(servletRequest.getProtocol() + " " + this.statusCode + " " + HttpServerUtils.getPhraseFromStatus(this.statusCode) + "\r\n");
		return sb.toString();
	}
	
	private String resHeaders() {
		StringBuilder sb = new StringBuilder();
		for (String key : headers.keySet()) {
			List<String> values = headers.get(key);
			for (String value : values) {
				sb.append(key + ": " + value + "\r\n");
			}
 		}
		sb.append(resCookieHeader());
		return sb.toString();
	}
	
	private String resCookieHeader() {
		StringBuilder sb = new StringBuilder();	
		HttpSession session = servletRequest.getSession();
		if (cookies != null && servletRequest.hasSession()) {
			if (session.isNew()) {
				sb.append("set-cookie: ");
				sb.append("JSESSIONID = " + session.getId());
				sb.append(", ");
			}
			for (Cookie cookie : cookies) {
				sb.append(cookie.getName() + "=" + cookie.getValue());
				sb.append(", ");
			}
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 2); 	//remove last ", "
		}
		sb.append(System.lineSeparator());
		return sb.toString();
	}
	
	private String getCurrentDate() {
		SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format.format(new Date());
	}
	public class ResponsePrintWriter extends PrintWriter {

		public ResponsePrintWriter(Writer out, boolean autoflush) {
			super(out, autoflush);
		}
		public void write(int c) {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.write(c);
			super.flush();
		}
		
		public void write(String s) {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.write(s);
			super.flush();
		}
		public void write(char[] buf) {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.write(buf, 0, buf.length);
		}
		public void flush() {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.flush();

		}	
		public void print(String str) {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.print(str);
		}
		public void print(char c) {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.print(c);
		}
		public void print(char[] s) {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.print(s);
		}
		public void println(String str) {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.println(str);
		}
		public void println(char c) {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.println(c);
		}	
		public void println(char[] s) {
			if (isHeadersSent == false) {
				isHeadersSent = true;
				sendHeaders();
			}
			super.println(s);
		} 
		
		private void sendHeaders() {
			StringBuilder sb = new StringBuilder();
			sb.append(resInitLine());
			sb.append(resHeaders());
			sb.append("Date: " + getCurrentDate() + "\r\n");
			sb.append("Content-Type: " + getContentType() + "\r\n");
			sb.append("Content-Encoding: " + getCharacterEncoding() + "\r\n");
			sb.append("\r\n");
			super.print(sb.toString());
			super.flush();
			isCommitted = true;
			isHeadersSent = true;
		}
	}
}
