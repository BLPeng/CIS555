package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webserver.HTTPRequestParser.CODE;
import edu.upenn.cis.cis455.webserver.HTTPRequestParser.RequestLine;


public class WorkerThread extends Thread{

	private MyBlockingQueue<Socket> requestQueue;
	private Socket task;
	private Boolean run;
	private int label;
	
	public WorkerThread(MyBlockingQueue<Socket> requestQueue, int label){
		this.requestQueue = requestQueue;
		this.label = label;
	}
	public void run(){
		
		run = true;
		while (run){
			try {
				task = requestQueue.get();
				if (task.isConnected()){
					BufferedReader in = new BufferedReader(new InputStreamReader(task.getInputStream()));
					String initLine = in.readLine();
		//			handleRequest(task);
					PrintWriter out = new PrintWriter(task.getOutputStream(), true);
					out.println("HTTP/1.0 200 OK");
				}	
				task.close();
				task = null;
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			} finally{
				
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
		
		
	}
	
	private String responseMessage(HTTPRequestParser requestParser, CODE code){
		
		return " ";
		/*StringBuilder sb = new StringBuilder();
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
		*/
		
	}
	
	private void shutdownServer(){
		
		if (HttpServer.httpServer != null){
			HttpServer.shutdownServer();
		}
		
	}
	
	public void stopThread(){


	}
	
	private void closeSocket(){
		
		
	}

}
