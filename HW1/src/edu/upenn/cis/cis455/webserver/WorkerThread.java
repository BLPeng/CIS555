package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webserver.HTTPRequestParser.CODE;


public class WorkerThread extends Thread{

	private MyBlockingQueue<Socket> requestQueue;
	private Socket task;
	private Boolean run;
	private int label;
	static final Logger logger = Logger.getLogger(WorkerThread.class);	
	
	public WorkerThread(MyBlockingQueue<Socket> requestQueue, int label){
		this.requestQueue = requestQueue;
		this.label = label;
	}
	public void run(){
		
		run = true;
		while (run){
			try {
				task = requestQueue.get();
				logger.info("thread " +label+ ", Handle task");		
				handleRequest(task);

				closeSocket();		//close the socket
			} catch (InterruptedException e) {
				logger.error("Failed to get task");
				e.printStackTrace();
			}

		}
		
	}
	
	
	private void handleRequest(Socket socket){
		
		String res = "";
		HTTPRequestParser requestParser = new HTTPRequestParser();	
		CODE code = requestParser.parseHttpRequest(task);
		switch (code){
		case BADDIR:
			res = responseMessage(requestParser, code);
			responseToClient(res, socket);
			break;
		case BADREQ:
			res = responseMessage(requestParser, code);
			responseToClient(res, socket);
			break;
		case CONTROL:
			
			break;
		case SHUTDOWN:
			shutdownServer();
			res = responseMessage(requestParser, code);
			responseToClient(res, socket);			
			break;
		case NOFOUND:
			res = responseMessage(requestParser, code);
			responseToClient(res, socket);			
			break;
		case NORMAL:
			res = responseMessage(requestParser, code);
			responseToClient(res, socket);	
			break;
		case PARSE:
			break;
		default:
			break;
			
		}
		
	}
	
	
	private void responseToClient(String res, Socket socket){
		
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(res);
			out.close();
		} catch (IOException e) {
			logger.error("Error");
			e.printStackTrace();
		}
		
	}
	
	private String responseMessage(HTTPRequestParser requestParser, CODE code){
		
		StringBuilder sb = new StringBuilder();
		sb.append(requestParser.getInitialLine().protocol);
		sb.append(" ");
		switch (code){
		case BADREQ:{
			sb.append("400 "); sb.append(" Bad request method!");
			return sb.toString();
		}	
		case BADDIR:
			
			return "";
		case SHUTDOWN:{
			sb.append("200 "); sb.append(" Server successfully shutdown!");
			return sb.toString();
		}
		case NORMAL:{
			sb.append("200 "); sb.append(" OK!");
			return sb.toString();
		}
		
			
		default:
			return "";
		}
		
		
	}
	
	private void shutdownServer(){
		
		if (HttpServer.httpServer != null){
			HttpServer.shutdownServer();
		}
		
	}
	
	public void stopThread(){
		//TODO
		//let current work finish
		this.run = false;

	}
	
	private void closeSocket(){
		
		try {
			if (task != null){
				task.close();
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
