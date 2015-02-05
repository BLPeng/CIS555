package edu.upenn.cis.cis455.webserver;

import java.net.Socket;
import org.apache.log4j.Logger;


public class WorkerThread extends Thread{

	private MyBlockingQueue<Socket> requestQueue;
	private Boolean run;
	static final Logger logger = Logger.getLogger(WorkerThread.class);	
	
	public WorkerThread(MyBlockingQueue<Socket> requestQueue){
		this.requestQueue = requestQueue;
	}
	public void run(){
		
		run = true;
		while (run){
			
			try {
				Socket task = requestQueue.get();
				logger.info("Handle task");
			} catch (InterruptedException e) {
				logger.error("Failed to get task");
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void stopThread(){
		
		synchronized (run){
			this.run = false;
		}

	}

}
