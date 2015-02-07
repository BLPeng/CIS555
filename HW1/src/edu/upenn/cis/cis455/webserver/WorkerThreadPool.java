package edu.upenn.cis.cis455.webserver;

import java.net.Socket;

public class WorkerThreadPool{

	private final int threadPoolSize = 2;	//for multi-processor /core, increase this number
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
	
	public void shutdown(){

		for (int i = 0; i < threadPoolSize; i++){
			pools[i].terminate();
		}
	}
}
