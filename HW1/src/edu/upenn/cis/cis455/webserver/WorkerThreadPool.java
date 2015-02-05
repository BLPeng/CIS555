package edu.upenn.cis.cis455.webserver;

import java.net.Socket;

public class WorkerThreadPool extends Thread{

	private final int threadPoolSize = 200;
	//shared blockingQueue
	private MyBlockingQueue<Socket> requestQueue;
	private WorkerThread[] pools;
	
	public WorkerThreadPool(MyBlockingQueue<Socket> requestQueue){
		
		this.requestQueue = requestQueue;
		pools = new WorkerThread[threadPoolSize];
		for (int i = 0; i < threadPoolSize; i++){
			pools[i] = new WorkerThread(this.requestQueue);
		}
	}
	
	public void run(){
		
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].start();
		}
		
	}
	
	public void stopThreadPools(){

		for (int i = 0; i < threadPoolSize; i++){
			pools[i].stopThread();
		}
	}
}
