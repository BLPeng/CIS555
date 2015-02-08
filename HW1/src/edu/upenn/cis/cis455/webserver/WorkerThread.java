package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

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
				if (task.isConnected()){
					handleRequest(task);
/*					BufferedReader in = new BufferedReader(new InputStreamReader(task.getInputStream()));
					String initLine = in.readLine();			
					PrintWriter out = new PrintWriter(task.getOutputStream(), true);
					out.println("HTTP/1.0 200 OK");*/
				}	
				task.close();
				this.reqUrl = "None";
				task = null;
			} catch (InterruptedException e) {
				logger.error("Thread stopped");
			} catch (IOException e) {
				logger.error("Can not fetch/parse task");
			} 
		}	
	}
	
	
	private void handleRequest(Socket socket) throws IOException{
		
		HTTPRequestParser requestParser = new HTTPRequestParser();	
		requestParser.parseHttpRequest(task);
		this.reqUrl = requestParser.getUrl();
		
		CODE code = requestParser.getCode();
		if (code == CODE.SHUTDOWN) {		//special url
			shutdownServer();	
		} 
		String res  = genResMessage(requestParser, code);
		responseToClient(res, socket);
	}
	
	
	private void responseToClient(String res, Socket socket){	
		PrintWriter out;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(res);
		} catch (IOException e) {
			logger.error("Could not write to client");
		}		
	}
	
	private String genResMessage(HTTPRequestParser requestParser, CODE code){
	
		StringBuilder sb = new StringBuilder();
		String protocol = requestParser.getProtocol();
		sb.append(protocol);
		sb.append(" ");
		switch (code) {
		case BADREQ:{
			sb.append("400 "); sb.append(" Bad request method!");
			return sb.toString();
		}	
		case BADDIR:{
			sb.append("403 "); sb.append(" Bad request directory!");
			sb.append("\r\n");
			sb.append("<h1>Bad request directory!</h1>");
			return "";
		}
		case SHUTDOWN:{
			sb.append("200 "); sb.append(" Server successfully shutdown!\n");
			sb.append("\r\n");
			sb.append("<h1>Server successfully shutdown</h1>");
			sb.append(System.getProperty("line.separator"));
			return sb.toString();
		}
		case CONTROL:{
			sb.append("200 "); sb.append(" Server status");
			sb.append(System.getProperty("line.separator"));
			sb.append("\r\n");
			sb.append(genHTMLPage(getControlPage()));
			return sb.toString();	
		}
		case NORMAL:{
			sb.append("200 "); sb.append(" Normal request");
			sb.append(System.getProperty("line.separator"));
			sb.append("\r\n");
			sb.append("<h1>Not implemented yet</h1>");
			return sb.toString();	
		}
		case LISTDIR:{
			File folder = new File(reqUrl);
			String[] files = folder.list();
			return genFileListPage(files);
		}
		default:
			sb.append("200 "); sb.append(" Not implemented yet!");
			return sb.toString();
		}
	}
	
	
	

	
	private void shutdownServer() {	
		if (HttpServer.httpServer != null){
			HttpServer.shutdownServer();
		}	
	}
	
	// generate HTML page
	private String genHTMLPage(String body){
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
		sb.append("<th> URL    </th>");
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
