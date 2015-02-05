package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

public class HTTPRequestParser {
	
	static final Logger logger = Logger.getLogger(HTTPRequestParser.class);	
	private RequestLine initialLine;	//first line of req
	private CODE code;			//parse code
		
	public RequestLine getInitialLine() {
		return initialLine;
	}

	public CODE getCode() {
		return code;
	}

	public enum CODE {
		BADREQ, BADDIR, SHUTDOWN, CONTROL, PARSE, NONE, NOFOUND
	}
	
	public CODE parseHttpRequest(Socket socket){
		
		BufferedReader in;

		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			//handle the initial line
			String initLine = in.readLine();
			initialLine = new RequestLine(initLine);
			if (initialLine.valid == false){
				code = CODE.BADREQ;
				return CODE.BADREQ;
			}	
			
			if (filterInvalidRequest(initialLine)){
				code = CODE.BADREQ;
				return CODE.BADREQ; 
			}
			
			if ("/shutdown".equals(initialLine.url.toLowerCase())){
				//shotdown server
				code = CODE.SHUTDOWN;
				return CODE.SHUTDOWN; 
			}
			
			if ("/control".equals(initialLine.url.toLowerCase())){
				code = CODE.CONTROL;
				return CODE.CONTROL;
			}
			
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		return CODE.NONE;
		
	}
	

	// filter out invalid request
	private boolean filterInvalidRequest(RequestLine initialLine){
		
	    System.out.println(initialLine.oriStr);
		if (!"GET".equals(initialLine.method) && !"HEAD".equals(initialLine.method) ){
			return true;
		}
		//TODO
		return false;
	}
	
	class RequestLine{
		
		String method;
		String url;
		String protocol;
		String oriStr;
		boolean valid;
		
		public RequestLine(String line){
			
			oriStr = line;
			valid = true;
			String[] tmp = line.split(" ");
			if (tmp.length != 3){
				valid = false;
			}else{
				method = tmp[0];
				url = tmp[1];
				protocol = tmp[2];
			}
		}
	
	}
	
}
