package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webserver.HTTPRequestParser.CODE;
import edu.upenn.cis.cis455.webserver.WorkerThreadPool.ThreadStats;


public class WorkerThread extends Thread{

	private MyBlockingQueue<Socket> requestQueue;
	private WorkerThreadPool threadPool;
	private String reqUrl;
	private Socket task;
	private Boolean run;
	private int label;
	static final Logger logger = Logger.getLogger(WorkerThread.class.getName());
	
	public WorkerThread(MyBlockingQueue<Socket> requestQueue, WorkerThreadPool pool, int label){
		super("Thread " + String.valueOf(label));
		this.requestQueue = requestQueue;
		this.label = label;
		this.threadPool = pool;
		this.reqUrl = "None";
	}
	public void run(){
		
		run = true;
		while (run){
			try {
				task = requestQueue.get();
				if (!task.isClosed()){	
					task.setSoTimeout(100000);
					handleRequest(task);
/*					System.out.println("dasf");
					task.setSoTimeout(5000);
					BufferedReader in = new BufferedReader(new InputStreamReader(task.getInputStream()));
					String initLine = in.readLine();
					System.out.println("sdf");
					PrintWriter out = new PrintWriter(task.getOutputStream(), true);
					out.println("HTTP/1.0 200 OK");		*/		
				}		
				this.reqUrl = "None";	
			} catch (InterruptedException e) {
				logger.error("Thread stopped");
			} catch (IOException e) {
		//		e.printStackTrace();
		//		logger.error("Can not fetch/parse task");

			} finally {
				try {
					//why chrome send two requests with second one empty???????????
					if (task != null)
						task.close();
					task = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}	
	}
	
	// 200-level-code need this check 
	private String checkModifyHeader(HTTPRequestParser requestParser) {			
		HashMap<String, String> headers = requestParser.getHeaders();
		String url = HttpServer.rootDir + requestParser.getUrl();
		String ret = "200";
		if (headers.containsKey("If-Modified-Since:".toLowerCase(Locale.ENGLISH))) {
			if ("GET".equalsIgnoreCase(requestParser.getMethod())) {
				Date date = convertDataFormat(headers.get("If-Modified-Since:".toLowerCase(Locale.ENGLISH)));
				File file = new File(url);
				if (date != null && file != null && date.getTime() > file.lastModified()) {
					return "304";
				}
			}
		}else if(headers.containsKey("If-Unmodified-Since:".toLowerCase(Locale.ENGLISH))) {
			Date date = convertDataFormat(headers.get("If-Unmodified-Since:".toLowerCase(Locale.ENGLISH)));
			File file = new File(url);
			if (date != null && file != null && date.getTime() < file.lastModified()) {
				return "412";
			}
		}
		return ret;
	}
	
	private Date convertDataFormat(String dateStr) {
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
	
	
	private void handleRequest(Socket socket) throws IOException{
		
		HTTPRequestParser requestParser = new HTTPRequestParser();	
		requestParser.parseHttpRequest(task);
		if (requestParser.getUrl() == null){	//set request url for later use
			this.reqUrl = "None";
		}else{
			this.reqUrl = HttpServer.rootDir + requestParser.getUrl();
		}
		CODE code = requestParser.getCode();
		if (code == CODE.SHUTDOWN) {		//special url
			shutdownServer();	
		} 
		if (code == CODE.FILE) {
			resFileContent(requestParser, socket);
		} else {
			String res  = genResMessage(requestParser);
			responseToClient(res, socket);			
		}

	}
	
	
	private void responseToClient(String res, Socket socket){	
		PrintWriter out;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(res);
	//		logger.info("Send to client");
		}catch (IOException e) {
			logger.error("Could not write to client");
		}		
	}
	
	private String genResMessage(HTTPRequestParser requestParser){	
		CODE code = requestParser.getCode();
		String method = requestParser.getMethod();
		String protocol = requestParser.getProtocol();
		String content = null;
		switch (code) {
		case BADHEADER1: {	//unknown header
			content = "<h1>Unknown headers</h1>";
			return genResponse(method, protocol, "400", "Unknown headers!", content);
		}
		case BADHEADER2: {	//not host header in http/1.1
			content = "<h1>HTTP/1.1 without host header!</h1>";
			return genResponse(method, protocol, "400", "HTTP/1.1 without host header!", content);
		}
		case NOALLOW: {
			content = "<h1>Request method not allowed!</h1>";
			return genResponse(method, protocol, "405", "Request method not allowed!", content);
		}	
		case NOIMPLEMENT: {
			content = "<h1>Request method not implemented!</h1>";
			return genResponse(method, protocol, "501", "Request method not implemented!", content);
		}	
		case NOFOUND: {
			content = "<h1>Resource no found!</h1>";
			return genResponse(method, protocol, "404", "Resource no found!", content); 
		}
		case BADDIR: {
			content = "<h1>Bad request directory!</h1>";
			return genResponse(method, protocol, "403", "Bad request directory!", content); 
		}
		case SHUTDOWN:{
			content = "<h1>Server successfully shutdown!</h1>";
			return genResponse(method, protocol, "200", "Server successfully shutdown!", content); 
		}
		case CONTROL:{
			//avoid extra work
			if (!"HEAD".equalsIgnoreCase(method))	content = genHTTPContent(getControlPage());
			return genResponse(method, protocol, "200", "Server status", content); 
		}
/*		case NORMAL:{
			content = "<h1>Feature not implemented yet</h1>";
			return genResponse(method, protocol, "200", "Normal request", content); 
		}*/
		case LISTDIR:{
			if ("304".equalsIgnoreCase(checkModifyHeader(requestParser))) {
				return genResponse(method, protocol, "304", "Not Modified", null);
			}else if ("412".equalsIgnoreCase(checkModifyHeader(requestParser))) {
				return genResponse(method, protocol, "412", "Precondition Failed", null);
			}else {
				//avoid extra work
				if (!"HEAD".equalsIgnoreCase(method)) {
					File folder = new File(reqUrl);
					String[] files = folder.list();
					content = genHTTPContent(genFileListPage(files));
				}
				return genResponse(method, protocol, "200", "List files", content);
			}
		}
		default:{
			content = "<h1>Invalid request</h1>";
			return genResponse(method, protocol, "400", "Invalid request", content); 
		}
		}
	}
	
	
	private String genResponse(String method, String protocol, String code, String reasonPhrase, String body) {
		StringBuilder sb = new StringBuilder();
		sb.append(protocol + " ");
		sb.append(code + " "); sb.append(reasonPhrase);
		sb.append(System.getProperty("line.separator"));
		sb.append("Server : Java/CIS455-v1.0");
		sb.append(System.getProperty("line.separator"));
		sb.append("Date : "); sb.append(getServerDate());
		sb.append(System.getProperty("line.separator"));		//not support yet
		sb.append("Connection: close"); 
		sb.append(System.getProperty("line.separator"));
		sb.append("Last-Modified: " + getLastModifiedTime(this.reqUrl));		//last-modified
		sb.append(System.getProperty("line.separator"));
		sb.append("\r\n");
		if ((!"HEAD".equalsIgnoreCase(method)) && body != null)		//if not head mothod
			sb.append(body);
//		System.out.println(body);
//		System.out.println("Response size: " + body.length());
		return sb.toString();
	}
	
	private void resFileContent(HTTPRequestParser requestParser, Socket socket) throws IOException {
		String protocol = requestParser.getProtocol();
		String file = this.reqUrl;
		String ext = getFileExt(file);
		PrintStream pstream = new PrintStream(socket.getOutputStream(), true);
		if (ext == null) {	// not a file
			pstream.write((protocol + " 404 Resource not found.\r\n").getBytes(Charset.forName("UTF-8")));
			pstream.write("\r\n".getBytes(Charset.forName("UTF-8")));
			return;
		}
		String code = checkModifyHeader(requestParser);
		if ("304".equalsIgnoreCase(code)) {
			pstream.write((protocol + " 304 Not Modified\r\n").getBytes(Charset.forName("UTF-8")));
			pstream.write(("Date : " + getServerDate() + "\r\n").getBytes(Charset.forName("UTF-8")));
			pstream.write("\r\n".getBytes(Charset.forName("UTF-8")));
		}else if ("412".equalsIgnoreCase(code)) {
			pstream.write((protocol + " 412 Precondition Failed\r\n").getBytes(Charset.forName("UTF-8")));
			pstream.write("\r\n".getBytes(Charset.forName("UTF-8")));
			return;
		}
		String fileType = HttpServer.fileTypes.get(ext);
		pstream.write((protocol + " 200 File request\r\n").getBytes(Charset.forName("UTF-8")));
		// headers 
		pstream.write(("Server : Java/CIS455-v1.0\r\n").getBytes(Charset.forName("UTF-8")));
		pstream.write(("Date : " + getServerDate() + "\r\n").getBytes(Charset.forName("UTF-8")));
		pstream.write(("Connection: close\r\n").getBytes(Charset.forName("UTF-8"))); 
		pstream.write(("Last-Modified: " + getLastModifiedTime(this.reqUrl) + "\r\n").getBytes(Charset.forName("UTF-8")));		//last-modified
		
		if (fileType == null) {
			// Unknown file type MIME?, return binary data
			pstream.write("Content-Type : application/octet-stream\r\n".getBytes(Charset.forName("UTF-8")));
		} else {
			pstream.write(("Content-Type : " + fileType + "\r\n").getBytes(Charset.forName("UTF-8")));
		}
		pstream.write("\r\n".getBytes(Charset.forName("UTF-8")));
		if (fileType == null || ".gif".equals(ext) || ".png".equals(ext) || ".jpg".equals(ext)) {
			readBinaryContent(pstream, this.reqUrl);
		} else {
			readFileContent(pstream, this.reqUrl);
		}
//		System.out.println("byte array");
		pstream = null;
	}
	
	private void readBinaryContent(PrintStream pstream ,String url) throws IOException {
		File fi = new File(url);
		byte[] fileContent = Files.readAllBytes(fi.toPath());
		pstream.write(fileContent);
		pstream.flush();
	}
	
	private void readFileContent(PrintStream pstream, String url) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(url)); 
		try
		{
			String line = null;
		    while((line = br.readLine()) != null)
		    {
		    	pstream.println(line);
		    }
		    br.close();
		}catch(Exception ex) {
			//
		}finally {
			br.close();
		}
	}
	
	
	private String getServerDate() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}
	
	private void shutdownServer() {	
		if (HttpServer.httpServer != null){
			HttpServer.shutdownServer();
		}	
	}
	
	// generate HTML page
	private String genHTTPContent(String body){
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

	
	private String getFileExt(String reqUrl) {
		int idx = reqUrl.lastIndexOf(".");
		if (idx == -1 || idx == 0)	return null;
		else return reqUrl.substring(idx);
	}
	
	private String getLastModifiedTime(String fileUrl) {
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
	
	private String genFileListPage(String[] files) {
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
	
	private String getControlPage() {
		StringBuilder sb = new StringBuilder();
		List<ThreadStats> status = threadPool.getThreadStatus();
		if (threadPool == null)	return "";
		sb.append("<h1>Server status</h1>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<h2>Xiaobin Chen, Seas: xiaobinc</h2>");
		sb.append("<h3>Thread pool size: " + status.size() + "</h3>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<table>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<tr>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<th>Thread    </th>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<th>Status     </th>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<th> URL    </th>");
		sb.append(System.getProperty("line.separator"));
		sb.append("</tr>");
		sb.append(System.getProperty("line.separator"));
		
		for (ThreadStats tmp : status){
			sb.append("<tr>");
			sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + tmp.threadName + "</td>");
			sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + tmp.threadStatus.toString() + "</td>");
			sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + tmp.reqUrl + "</td>");
			sb.append(System.getProperty("line.separator"));
			sb.append("</tr>");
			sb.append(System.getProperty("line.separator"));
		}	
		sb.append("</table>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<a href=\"/shutdown\">");
		sb.append("<button>Shutdown</button></a>");
		return sb.toString();
	}
	
	public String getProcUrl() {
		return this.reqUrl;
	}
	
	
	public void terminate() {
		logger.info("Thread " + this.label + " terminated.");
		this.run = false;
		this.interrupt();
	}
}
