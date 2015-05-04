package edu.upenn.cis455.crawler.info;

public class WorkerStatus {
	private String ip;
	private int port;
	private String status;
	public String getIPPort() {
		return getIp() + ":" + getPort();
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		if (port == 0) {
			return 80;
		}
		return port; 
	}
	public void setPort(int port) {
		this.port = port;
	}

	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}

	public WorkerStatus() {
		
	}
	public WorkerStatus(String ip, int port, String job, String status, long keysRead, long keysWrite) {
		this.ip = ip;
		this.port = port;
		this.status = status;
	}
	public String toString() {
		return "IP/Port: " + this.ip + ":" + this.port +
				" status: " + this.status ;
	}
}
