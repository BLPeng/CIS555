package edu.upenn.cis.cis455.webserver;

/*
 * todo
 * class:
 * 1. HtteServer.java : start all threads
 * 2. MyBlockingQueue.java : blockingQueue to hold incoming requests
 * 3. WorkerThread.java : thread to handle request
 * 4. WorkerThreadPool.java : thread pool of WorkerThreads , consumer
 * 5. RequestReceiver.java : receive requests, producer
 */
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;


class HttpServer {
	
	public static HttpServer httpServer; 
	public static int portNumber;
	public static String rootDir;
	public static String lastModified;
	public static HashMap<String, String> fileTypes;
	public static HashSet<String> acceptMethods;
	private final int blockingQueueSize = 2000;
	private RequestReceiver requestReceiver;
	private WorkerThreadPool workerThreadPool;	
	private MyBlockingQueue<Socket> blockingQueue;
	
	static final Logger logger = Logger.getLogger(HttpServer.class.getName());
	
	public HttpServer(String portNum, String root) throws IOException{
		
		HttpServer.httpServer = this;
		int port = Integer.valueOf(portNum);
		portNumber = port;
		rootDir = root;
		blockingQueue = new MyBlockingQueue<Socket>(blockingQueueSize);
		requestReceiver = new RequestReceiver(portNumber, rootDir, blockingQueue);
		workerThreadPool = new WorkerThreadPool(blockingQueue);
		fileTypes = new HashMap<String, String>();
		fileTypes.put(".jpg", "image/jpeg");
		fileTypes.put(".txt", "text/plain");
		fileTypes.put(".html", "text/html");
		fileTypes.put(".png", "image/png");
		fileTypes.put(".gif", "image/gif");
		fileTypes.put(".css", "text/css");
		fileTypes.put(".pdf", "application/pdf");
		fileTypes.put(".ico", "image/x-icon");
		
		acceptMethods = new HashSet<String>();
		acceptMethods.add("GET");
		acceptMethods.add("HEAD");
		acceptMethods.add("POST");
		acceptMethods.add("PUT");
		acceptMethods.add("DELETE");
		acceptMethods.add("CONNECT");
		acceptMethods.add("OPTIONS");
		acceptMethods.add("TRACE");
		
		Calendar calendar = Calendar.getInstance();		//server start time
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		lastModified = dateFormat.format(calendar.getTime());
	}
	
    public static void main(String args[])
    {
    	logger.info("Starting HttpServer.");
    	// check if input is invalid
		if (!validateInput(args)){
			System.out.println("Arguments are not valid");
			return;
		}
		HttpServer httpServer;
		try {
			httpServer = new HttpServer(args[0], args[1]);
			httpServer.runServer();
		} catch (IOException e) {
			logger.error("Could not initialize server");
			e.printStackTrace();
		}
		
    }
    
    private static boolean validateInput(String[] args){
		
		//validate inputs
    	if (args != null && args.length == 0) {
    		System.out.println("Full name: Xiaobin Chen. Seas login name: xiaobinc");
    		return false;
    	}
    	
		if (args == null || args.length != 2) {	
			logger.error("Number of arguments is wrong!");
			return false;
		}
		
		//validate port number
		int portNum = Integer.valueOf(args[0]);
		if (portNum < 0 || portNum >= 65536){
			logger.error("Port Number is invalid!");
			System.out.println("Valid port number range:[0, 65535], [1024, 65535] is recommended!");
			return false;
		}

		System.out.println("Server Name: Xiaobin Chen. PennKey: xiaobinc");
        System.out.println("Port: " + portNum + "\rRoot Directory: " + args[1]);
		return true;

	}
	//let's run server
	private void runServer() {

		requestReceiver.start();
		workerThreadPool.start();
		
	}
    
	
	//let's shutdown server  
	public static void shutdownServer() {
		
		if (httpServer != null){
			logger.info("shutdown server.");
			httpServer.requestReceiver.shutdown();
			httpServer.workerThreadPool.shutdown();
		}
		
	}
	
}
