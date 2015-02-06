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
import java.net.Socket;

import org.apache.log4j.Logger;


class HttpServer {
	
	public static HttpServer httpServer; 
	public static Boolean run;
	private int portNumber;
	private String rootDir;
	private final int blockingQueueSize = 1000;
	private RequestReceiver requestReceiver;
	private WorkerThreadPool workerThreadPool;	
	private MyBlockingQueue<Socket> blockingQueue;
	
	static final Logger logger = Logger.getLogger(HttpServer.class.getName());
	
	public HttpServer(String portNum, String root){
		
		HttpServer.httpServer = this;
		int port = Integer.valueOf(portNum);
		portNumber = port;
		rootDir = root;
		blockingQueue = new MyBlockingQueue<Socket>(blockingQueueSize);
		requestReceiver = new RequestReceiver(portNumber, rootDir, blockingQueue);
		workerThreadPool = new WorkerThreadPool(blockingQueue);
	}
	
    public static void main(String args[])
    {
    	logger.info("Starting HttpServer.");
    	// input invalid
		if (!validateInput(args)){
			//TODO return error res
		}
		HttpServer httpServer = new HttpServer(args[0], args[1]);
		httpServer.runServer();
    }
    
    private static boolean validateInput(String[] args){
		
		//validate inputs
		if (args == null || args.length != 2){	
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
	
	private void runServer(){

		run = true;
		requestReceiver.start();
		workerThreadPool.start();
		
	}
    
	
	//  
	public static void shutdownServer(){
		
		HttpServer hs = HttpServer.httpServer;
		if (hs != null){
			logger.info("shutdown server.");
			hs.requestReceiver.shutdown();
			hs.workerThreadPool.shutdownThreadPools();
		}
		
	}
}
