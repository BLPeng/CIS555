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
	
	private static int portNumber;
	private static String rootDir;
	private final static int blockingQueueSize = 1000;
	private static MyBlockingQueue<Socket> blockingQueue;
	
	static final Logger logger = Logger.getLogger(HttpServer.class.getName());
	
    public static void main(String args[])
    {
    	logger.info("Starting HttpServer.");
    	// input invalid
		if (!validateInput(args)){
			return;
		}
        runServer();
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
		portNumber = portNum;
		rootDir = args[1];
		System.out.println("Server Name: Xiaobin Chen. PennKey: xiaobinc");
        System.out.println("Port: " + portNumber + "\rRoot Directory: " + rootDir);
		return true;
		//validate root dir
		

	}
	
	private static void runServer(){
		
		blockingQueue = new MyBlockingQueue<Socket>(blockingQueueSize);
		RequestReceiver rr = new RequestReceiver(portNumber, rootDir, blockingQueue);
		WorkerThreadPool wtp = new WorkerThreadPool(blockingQueue);
		wtp.start();
		rr.start();
	}
  
}
