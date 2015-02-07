package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class HTTPRequestParser {
	
	private String protocol;
	private String reqUrl;
	private String method;
	private List<String> headers;
//	private String body;				do not deal with body
	private CODE code;			
		
	public String getProtocol() {
		return this.protocol;
	}
	
	public HTTPRequestParser() {
		code = CODE.BADREQ;
		headers = new ArrayList<String>();
	}
	
	public CODE getCode() {
		return code;
	}

	public enum CODE {
		BADREQ, BADDIR, SHUTDOWN, CONTROL, NOFOUND, HEAD
	}
	
	public void parseHttpRequest(Socket socket) throws IOException{
		
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//parse the initial line
		String line = in.readLine();
		parseInitialLine(line);
		//get the headers
		while ((line = in.readLine()) != null) {
			if (line.length() == 0){
				break;
			}
			headers.add(line);
		}	
		filterRequest();	
	}
	

	// filter out invalid request
	private void filterRequest(){
		
		// not GET or HEAD request
		if (!"GET".equalsIgnoreCase(this.method) && !"HEAD".equalsIgnoreCase(this.method) ){
			this.code = CODE.BADREQ;
			return;
		}
		// security check
		// shutdown url
		if ("/shutdown".equalsIgnoreCase(this.reqUrl)){
			this.code = CODE.SHUTDOWN;
		}
		// control url
		if ("/control".equalsIgnoreCase(this.reqUrl)){
			this.code = CODE.CONTROL;
		}
	}
	
	private void parseInitialLine(String line){	
		if (line == null) this.code = CODE.BADREQ;
		else {
			String[] tmp = line.split(" ");
			if (tmp.length != 3){
				this.code = CODE.BADREQ;
			}else{
				method = tmp[0];
				reqUrl = tmp[1];
				protocol = tmp[2];
			}
		}
	}
	
}
