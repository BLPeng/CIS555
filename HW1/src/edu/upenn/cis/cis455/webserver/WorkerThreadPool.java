package edu.upenn.cis.cis455.webserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WorkerThreadPool {

	private final int threadPoolSize = 10;	//for multi-processor /core, increase this number
	private WorkerThread[] pools;
	
	public WorkerThreadPool(MyBlockingQueue<Socket> requestQueue) { 
		
		pools = new WorkerThread[threadPoolSize];
		for (int i = 0; i < threadPoolSize; i++){
			pools[i] = new WorkerThread(requestQueue, this, i);
		}
	}
	
	public void start() {		
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].start();
		}		
	}
	
	public List<ThreadStats> getThreadStatus() {
		if (threadPoolSize != pools.length)	return null;
		List<ThreadStats> status = new ArrayList<ThreadStats>();
		for (int i = 0; i < threadPoolSize; i++){
			ThreadStats ts = new ThreadStats(pools[i].getName(), pools[i].getState(), pools[i].getProcUrl());
			status.add(ts);
		}
		return status;
	}
	
	public void shutdown() {
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].terminate();
		}
	}
	
    public class ThreadStats {
    	String threadName;
    	Thread.State threadStatus;
    	String reqUrl;
    	public ThreadStats(String name, Thread.State status, String url) {
    		this.threadName = name;
    		this.threadStatus = status;
    		this.reqUrl = url;
    	}
    }
}
