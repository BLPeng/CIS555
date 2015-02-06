package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class RequestReceiver extends Thread{
	
	private int portNumber;
	private String rootDir;
	private Boolean acceptRequest = true;
	private ServerSocket serverSocket;
	private final int serverSocketSize = 300;
	//shared blockingQueue
	private MyBlockingQueue<Socket> bq;
	
	static final Logger logger = Logger.getLogger(RequestReceiver.class.getName());
	
	public int getPortNumber() {
		return portNumber;
	}
	public void setPortNumber(int portNum) {
		portNumber = portNum;
	}
	public String getRootDir() {
		return rootDir;
	}
	public void setRootDir(String Dir) {
		rootDir = Dir;
	}
	
	public RequestReceiver(int port, String dir, MyBlockingQueue<Socket> bq){
		
		portNumber = port;
		rootDir = new String(dir);
		this.bq = bq;
		acceptRequest = true;
		
	}
	
	public void run() {
		
		try {
			serverSocket = new ServerSocket(portNumber, serverSocketSize);
			Socket socket = null;
			//while accepting requests
			while (acceptRequest){
//				System.out.print(acceptRequest);
				try{
					socket = serverSocket.accept();
					bq.add(socket);
				}catch (IOException e) {
					logger.info("Socket closed");
				} 

			}
			
		} catch (IOException e) {
			logger.error("Socket error:" + portNumber);
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			logger.error("Could not add task to queue.");
			e.printStackTrace();
		} 
    }
	
	public void shutdown(){
		//stop accepting requests		
		this.acceptRequest = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.error("Can not close serverSocket");
			e.printStackTrace();
		}
	}
}
