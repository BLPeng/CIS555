package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webserver.HTTPRequestParser.CODE;


public class WorkerThread extends Thread{

	private MyBlockingQueue<Socket> requestQueue;
	private Socket task;
	private Boolean run;
	private int label;
	static final Logger logger = Logger.getLogger(WorkerThread.class.getName());
	
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
					handleRequest(task);
/*					BufferedReader in = new BufferedReader(new InputStreamReader(task.getInputStream()));
					String initLine = in.readLine();			
					PrintWriter out = new PrintWriter(task.getOutputStream(), true);
					out.println("HTTP/1.0 200 OK");*/
				}	
				task.close();
				task = null;
			} catch (InterruptedException e) {
				logger.error("Can not read from socket, socket closed?");
			} catch (IOException e) {
				logger.error("Can not fetch/parse task");
			} finally{
				
			}

		}
		
	}
	
	
	private void handleRequest(Socket socket) throws IOException{
		
		String res = "";
		HTTPRequestParser requestParser = new HTTPRequestParser();	
		requestParser.parseHttpRequest(task);
		CODE code = requestParser.getCode();
		if (code == CODE.SHUTDOWN){
			shutdownServer();	
		}	
		res = genResMessage(requestParser, code);
		responseToClient(res, socket);
	}
	
	
	private void responseToClient(String res, Socket socket) throws IOException{
		
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		out.println(res);
		
	}
	
	private String genResMessage(HTTPRequestParser requestParser, CODE code){
	
		StringBuilder sb = new StringBuilder();
		String protocol = requestParser.getProtocol();
		sb.append(protocol);
		sb.append(" ");
		switch (code){
		case BADREQ:{
			sb.append("400 "); sb.append(" Bad request method!");
			return sb.toString();
		}	
		case BADDIR:
			sb.append("404 "); sb.append(" Bad request directory!");
			return "";
		case SHUTDOWN:{
			sb.append("200 "); sb.append(" Server successfully shutdown!");
			return sb.toString();
		}
		
		default:
			sb.append("200 "); sb.append(" Not implemented yet!");
			return sb.toString();
		}
		
	}
	
	private void shutdownServer() {
		
		if (HttpServer.httpServer != null){
			HttpServer.shutdownServer();
		}
		
	}
	
	public void terminate() {
		logger.info("Thread " + this.label + " terminated.");
		this.run = false;
		this.interrupt();
	}
	
}
