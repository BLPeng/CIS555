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
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webservletcontainer.FakeContext;
import edu.upenn.cis.cis455.webservletcontainer.Handler;

class HttpServer {
	
	public static HttpServer httpServer; 
	public static int portNumber;
	public static String rootDir;
	public static String lastModified;
	public static HashMap<String, String> fileTypes;
	public static HashSet<String> acceptMethods;
	public static FakeContext fContext;
	private final int blockingQueueSize = 2000;
	private RequestReceiver requestReceiver;
	private WorkerThreadPool workerThreadPool;	
	private MyBlockingQueue<Socket> blockingQueue;
	
	static final Logger logger = Logger.getLogger(HttpServer.class.getName());
	
	public HttpServer(String portNum, String root, Handler handler) throws IOException{
		
		HttpServer.httpServer = this;
		int port = Integer.valueOf(portNum);
		portNumber = port;
		rootDir = root;
		blockingQueue = new MyBlockingQueue<Socket>(blockingQueueSize);
		requestReceiver = new RequestReceiver(portNumber, rootDir, blockingQueue);
		workerThreadPool = new WorkerThreadPool(blockingQueue);
		initBasicSetting();		//for file type and method type
		
		Calendar calendar = Calendar.getInstance();		//server start time
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		lastModified = dateFormat.format(calendar.getTime());
		
		// for ms2
		this.fContext = createContext(handler);
		System.out.println(fContext.getRealPath("/dx"));
	}
	
    public static void main(String args[]) throws Exception
    {
    	logger.info("Starting HttpServer.");
    	// check if input is invalid
		validateInput(args);
		Handler h = parseWebdotxml(args[2]);
		HttpServer httpServer;
		try {
			httpServer = new HttpServer(args[0], args[1], h);
			httpServer.runServer();
		} catch (IOException e) {
			logger.error("Could not initialize server");
			e.printStackTrace();
		}
		
    }
    
    private static void validateInput(String[] args){
		
		//validate inputs
    	if (args.length == 0) {
    		System.out.println("Full name: Xiaobin Chen. Seas login name: xiaobinc");
    		System.exit(-1);
    	}
    	//at lease three arguments
    	if (args.length < 3 || args.length % 2 == 0) {
			usage();
			System.exit(-1);
		}
		
		//validate port number
		int portNum = Integer.valueOf(args[0]);
		if (portNum < 0 || portNum >= 65536){
			logger.error("Port Number is invalid!");
			System.out.println("Valid port number range:[0, 65535], [1024, 65535] is recommended!");
			System.exit(-1);
		}

		System.out.println("Server Name: Xiaobin Chen. PennKey: xiaobinc");
        System.out.println("Port: " + portNum + "\rRoot Directory: " + args[1]);

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
	private static void usage() {
		System.err.println("usage: java HttpServer <path to web.xml> " 
				+ "[<GET|POST> <servlet?params> ...]");
	}
	// parse xml config file
	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	} 
	
	public void initBasicSetting() {
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
	}
	
	//set parameters and attributes
	private static FakeContext createContext(Handler h) {
		FakeContext fc = new FakeContext();
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		} 
		// attributes ???
		return fc;
	}
	
}
