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
	static final Logger logger = Logger.getLogger(WorkerThread.class);	
	
	public WorkerThread(MyBlockingQueue<Socket> requestQueue){
		this.requestQueue = requestQueue;
	}
	public void run(){
		
		run = true;
		while (run){
			
			try {
				task = requestQueue.get();
				logger.info("Handle task");
				HTTPRequestParser requestParser = new HTTPRequestParser();
				CODE code = requestParser.parseHttpRequest(task);
				reponseError(requestParser, code, task);
				
				
				
				closeSocket();
				
			} catch (InterruptedException e) {
				logger.error("Failed to get task");
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void stopThread(){
		//TODO
		synchronized (run){
			this.run = false;
		}

	}
	
	
	private void handleRequest(HTTPRequestParser requestParser, Socket socket){
		
		if (requestParser == null)	return;
		CODE code = requestParser.getCode();
		switch (code){
		case BADDIR:
		
			
		case BADREQ:
			
			
		}
		
	}
	
	
	private void reponseError(HTTPRequestParser requestParser, CODE code, Socket socket){
		
		StringBuilder sb = new StringBuilder();
		sb.append(requestParser.getInitialLine().method);
		sb.append(" ");
		switch (code){
		case BADREQ:{
			sb.append(" 400 "); sb.append(" Bad request method!");
			try {
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				String res = sb.toString();
				out.println(res);
				out.close();
			} catch (IOException e) {
				logger.error("Error");
				e.printStackTrace();
			}
			break;
		}
			
		case BADDIR:
		}
		
		
	}
	
	private void shutdownServer(Socket socket){
		
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
