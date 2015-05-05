package edu.upenn.cis455.crawler.info;

import java.util.Hashtable;

public class WorkerInfos {
	public Hashtable<String, WorkerStatus> workersStatus;
	public Hashtable<String, WorkerStatus> workersStatus1;
	private String masterIP;
	private int masterPort;
	public int port = 80;
	public WorkerInfos() {
		 workersStatus1 = new Hashtable<>();
		 workersStatus = new Hashtable<>();
	}
	
	public Hashtable<String, WorkerStatus> getWorkersStatus() {
		return workersStatus;
	}
	public void setWorkersStatus(Hashtable<String, WorkerStatus> workersStatus) {
		this.workersStatus = workersStatus;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	} 
	public void setPort(String port) {
		try {
	    	this.port = Integer.valueOf(port);
	    } catch (Exception e) {
	    	this.port = 80;
	    }
	} 
	public Hashtable<String, WorkerStatus> getWorkersStatus1() {
		return workersStatus1;
	}

	public void setWorkersStatus1(Hashtable<String, WorkerStatus> workersStatus1) {
		this.workersStatus1 = workersStatus1;
	}
}
