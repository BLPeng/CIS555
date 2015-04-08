package edu.upenn.cis455.mapreduce.infoclasses;

import java.util.Date;

public class WorkerStatus {
	private String ip;
	private int port;
	private String job;
	private String status;
	private long keysRead;
	private long keysWrite;
	private Date lastUpdated;
	public String getIPPort() {
		return ip + ":" + port;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public long getKeysRead() {
		return keysRead;
	}
	public void setKeysRead(long keyRead) {
		this.keysRead = keyRead;
	}
	public long getKeysWrite() {
		return keysWrite;
	}
	public void setKeysWrite(long keyWrite) {
		this.keysWrite = keyWrite;
	}
	public WorkerStatus() {
		
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
		this.lastUpdated = new Date();
	}
	public WorkerStatus(String ip, int port, String job, String status, long keysRead, long keysWrite) {
		this.ip = ip;
		this.port = port;
		this.job = job;
		this.status = status;
		this.keysRead = keysRead;
		this.keysWrite = keysWrite;
		this.lastUpdated = new Date();
	}
	public String toString() {
		return "IP/Port: " + this.ip + ":" + this.port +
				" status: " + this.status +
				" job: " + this.job +
				" keyRead: " + this.keysRead + 
				" keyWrite: " + this.keysWrite;
	}
}
