package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import edu.upenn.cis455.crawler.info.URLInfo;

public class HTTPClient {
	private String content;
	private String url;
	private String method;
	private String resCode;
	private final String USER_AGENT = "cis455crawler";
	private Map<String, List<String>> headers;
	private Map<String, List<String>> reqHeaders;
	private int type; 	//0 - http, 1 - https

	public HTTPClient() {
		reqHeaders = new HashMap<String, List<String>>();
		headers = new HashMap<String, List<String>>();
	}
	
	public void init() {
		content = null;
		url = null;
		method = "GET";
		reqHeaders = new HashMap<String, List<String>>();
		headers = new HashMap<String, List<String>>();
		resCode = null;
		initRequestHeaders(); 
	}
	
	private void initRequestHeaders() {		
		List<String> tmp = new ArrayList<String>();
		tmp.add(USER_AGENT);
		reqHeaders.put("User-Agent", tmp);
	}
	
	public void fetchContent() {
		if (type == 0) {
			getHttpContent(); 
		} else {
			getHttpsContent();
		}
	}
	
	public void getHttpsContent() {
		URL myURL;
		try {
			myURL = new URL(url);
			HttpsURLConnection connection = (HttpsURLConnection)myURL.openConnection();
			if (connection == null) {
				return;
			}
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod(method);
			for (String header : reqHeaders.keySet()) {
	        	for (String value : reqHeaders.get(header)) {
	        		connection.setRequestProperty(header, value);
	        	}
	        }
			resCode = String.valueOf(connection.getResponseCode());
			headers = connection.getHeaderFields();
			try {
				   StringBuilder sb = new StringBuilder();		
				   BufferedReader br = 
					new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
			 
				   String input;
			 
				   while ((input = br.readLine()) != null){
				      sb.append(input);
				      sb.append(System.lineSeparator());
				   }
				   br.close();
				   content = sb.toString();
				} catch (IOException e) {
				   e.printStackTrace();
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getHttpContent() {
		StringBuilder sb = new StringBuilder();
		boolean isXML = false;
		try {
			// http client
			URLInfo myURL = new URLInfo(url);
			Socket socket = new Socket(myURL.getHostName(), myURL.getPortNo());
			OutputStream theOutput = socket.getOutputStream();
	        PrintWriter out = new PrintWriter(theOutput, false);
	        String req = method + " " + url + " HTTP/1.0\r\n";
	        out.print(req); 
	        out.print("Host:" + myURL.getHostName() + "\r\n");
	        for (String header : reqHeaders.keySet()) {
	        	for (String value : reqHeaders.get(header)) {
	        		out.print(header + ": " + value + "\r\n");
	        	}
	        }
//	        out.print("Accept: application/xml, text/html, text/html, application/rss+xml\r\n");
			out.print("\r\n"); 
			out.flush(); 
			BufferedReader in = new BufferedReader(
			      new InputStreamReader(socket.getInputStream()));
			String line;
			boolean isHead = true;
			
			parseCode(line = in.readLine());
			while ((line = in.readLine()) != null) {
				if (isHead) {
					if (line.contains("xml")) {				//check header
						isXML = true;						
					}
					if (line.isEmpty()) {
						isHead = false;
					}
					parseHeaders(line);
				} else {
					sb.append(line);
					sb.append(System.lineSeparator());
				}
			}
			socket.close();
		}catch(Exception e) {
			return;
		}
		content = sb.toString();
	}
	
	private void parseHeaders(String line) throws IOException {
		line = line.trim();
		if (line.contains(":")) {				//the header line
			int idx = line.indexOf(":");
			String header = line.substring(0, idx).toLowerCase(Locale.ENGLISH);
			String value = line.substring(idx + 1).trim();
			if (headers.containsKey(header)) {
				headers.get(header).add(value);
			} else {
				List<String> values = new ArrayList<String>();
				values.add(value);
				headers.put(header, values);
			}
		} 
	}
	
	private void parseCode(String line) {
		if (line == null) {
			return;
		}
		line = line.trim();
		String[] tmp = line.split("\\s+");
		if (tmp.length < 3) {
			return;
		} else {
			resCode = tmp[1];
		}
	}
	
	public void setRequestHeaders(String header, String value) {
		if (reqHeaders.get(header) == null) {
			List<String> tmp = new ArrayList<String>();
			tmp.add(value);
			reqHeaders.put(header, tmp);
		} else {
			reqHeaders.get(header).add(value);
		}
	}
	
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public String getResCode() {
		return resCode;
	}

	public String getContent() {
		return this.content;
	}
	
	public void setURL(String url) {
		if (url == null || url.length() < 8) {
			return;
		}
		this.url = url.trim();
		if (url.toLowerCase().startsWith("http://")) {
			type = 0;
		} else if (url.toLowerCase().startsWith("https://")){
			type = 1;
		} else {
			return;
		}	
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public static Date convertDataFormat(String dateStr, int mode) {	//0 - normal, 1 - ifmodifiedsince/ifnotmodifiedsince	
		SimpleDateFormat format1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		format1.setTimeZone(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat format2 = new SimpleDateFormat("E, dd-MMM-yy HH:mm:ss z");
		format2.setTimeZone(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat format3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
		format3.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = null;
		try {
			date = format1.parse(dateStr);
		} catch (ParseException e) {
			date = null;
			if (mode == 0)
				return date;		//if mode == 0, normal, no need following format test
		}
		if (date == null) {
			try {
				date = format2.parse(dateStr);
			} catch (ParseException e) {
				date = null;
			}
		}
		if (date == null) {
			try {
				date = format3.parse(dateStr);
			} catch (ParseException e) {
				date = null;
			}
		}
		if (date.getTime() > System.currentTimeMillis()){
			return null;
		}
		return date;
	}
	
	public static String dateToString(Date date) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}
}
