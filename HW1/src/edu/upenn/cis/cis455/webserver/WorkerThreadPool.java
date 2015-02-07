package edu.upenn.cis.cis455.webserver;

import java.net.Socket;

public class WorkerThreadPool{

	private final int threadPoolSize = 1100;
	//shared blockingQueue
	private WorkerThread[] pools;
	
	public WorkerThreadPool(MyBlockingQueue<Socket> requestQueue){
		
		pools = new WorkerThread[threadPoolSize];
		for (int i = 0; i < threadPoolSize; i++){
			pools[i] = new WorkerThread(requestQueue, i);
		}
	}
	
	public void start(){
		
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].start();
		}
		
	}
	
	public void shutdownThreadPools(){

		for (int i = 0; i < threadPoolSize; i++){
			pools[i].stopThread();
		}
	}
}
