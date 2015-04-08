package edu.upenn.cis455.mapreduce.infoclasses;

import java.util.Date;

public class WorkerStatus {
	private String ip;
	private String port;
	private String job;
	private String status;
	private String keysRead;
	private String keysWrite;
	private Date lastUpdated;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
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
	public String getKeysRead() {
		return keysRead;
	}
	public void setKeysRead(String keyRead) {
		this.keysRead = keyRead;
	}
	public String getKeysWrite() {
		return keysWrite;
	}
	public void setKeysWrite(String keyWrite) {
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
	public WorkerStatus(String ip, String port, String job, String status, String keysRead, String keysWrite) {
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
				"keyRead: " + this.keysRead + 
				"keyWrite: " + this.keysWrite;
	}
}
