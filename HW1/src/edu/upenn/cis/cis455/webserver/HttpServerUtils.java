package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HttpServerUtils {
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
	
	public static String getServerDate() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}
	
	// generate HTML page
	public static String genHTTPContent(String body){
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");	sb.append(System.getProperty("line.separator"));
		sb.append("<head>");	sb.append(System.getProperty("line.separator"));
		sb.append("<title>Xiaobin Chen,  xiaobinc </title>");sb.append(System.getProperty("line.separator"));
		sb.append("<body>");	sb.append(System.getProperty("line.separator"));
		sb.append(body);
		sb.append("</body>");	sb.append(System.getProperty("line.separator"));
		sb.append("</head>");	sb.append(System.getProperty("line.separator"));
		sb.append("</html>");	sb.append(System.getProperty("line.separator"));
		return sb.toString();
	}
	
	public static String genFileListPage(String[] files) {
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>List files</h1>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<h2>Xiaobin Chen, Seas: xiaobinc</h2>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<table>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<tr>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<th>FileName    </th>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<th>URL    </th>");
		sb.append(System.getProperty("line.separator"));
		sb.append("</tr>");
		sb.append(System.getProperty("line.separator"));
		for (String file : files){
			sb.append("<tr>");
			sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + file + "</td>");
			sb.append(System.getProperty("line.separator"));
			sb.append("<td>" +  "<a href=\"" + file + "\"> " + file  + "</a> </td>");
			sb.append(System.getProperty("line.separator"));
			sb.append("</tr>");
			sb.append(System.getProperty("line.separator"));
		}	
		sb.append("</table>");
		return sb.toString();
	}
	
	public static String getLastModifiedTime(String fileUrl) {
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		File file = new File(fileUrl);
		if (file.isDirectory()) {
			return dateFormat.format(file.lastModified());		//return file last-modified time
		} else if (file.isFile()) {
			return dateFormat.format(file.lastModified());		//return file last-modified time
		} else {
			return HttpServer.lastModified;			//return server start time
		}
	}
	
	public static String getPhraseFromStatus(int status) {
		if (status == 200)	return "OK";
		else if (status == 500)	return "Internal Server Error";
		else if (status == 302) return "Redirect";
		else if (status == 100) return "Continue";
		else if (status == 202) return "Accepted";
		else if (status == 304) return "Not Modified";
		else if (status == 400) return "Bad Request";
		else if (status == 401) return "Unauthorized";
		else if (status == 403) return "Forbidden";
		else if (status == 404) return "Not Found";
		else if (status == 405) return "Method Not Allowed";
		else return "unknown";
	}
	
	public static String getFileContent(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		StringBuilder sb = new StringBuilder();
		try
		{
			String line = null;
		    while((line = br.readLine()) != null)
		    {
		    	sb.append(line);
		    	sb.append(System.lineSeparator());
		    }
		}catch(Exception ex) {
			//
		}finally {
			br.close();
		}
		return sb.toString();
	}
}
