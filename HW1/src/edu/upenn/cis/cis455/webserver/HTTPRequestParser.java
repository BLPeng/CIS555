package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
	
	public String getUrl() {
		return this.reqUrl;
	}
	
	public HTTPRequestParser() {
		code = CODE.NORMAL;
		headers = new ArrayList<String>();
	}
	
	public CODE getCode() {
		return code;
	}

	public enum CODE {
		BADREQ, BADDIR, SHUTDOWN, CONTROL, NOFOUND, HEAD, 
		LISTDIR, FILE, NORMAL
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
		if (parseURL(this.reqUrl) == null){
			this.code = CODE.BADDIR;
			return;
		}
		// shutdown url
		if ("/shutdown".equalsIgnoreCase(this.reqUrl)){
			this.code = CODE.SHUTDOWN;
		}
		// control url
		if ("/control".equalsIgnoreCase(this.reqUrl)){
			this.code = CODE.CONTROL;
		}
	}
	
	private String simplifyPath(String path) {
        if (path == null)   return null;
        int level = 0;
        Deque<String> st = new ArrayDeque<String>();
        String[] folder = path.split("/");
        for (int i = 0; i < folder.length; i++) {
             if (".".equals(folder[i]) || "".equals(folder[i])) continue;
             if ("..".equals(folder[i])){
                 level--;
                 st.pollLast();         //return null if empty
             }else{
                 if (level >= 0)
                    st.offer(folder[i]);
                 level++;
             }    
        }
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        while (st.size() > 0){
            sb.append(st.pollFirst());
            sb.append("/");
        }
        if (sb.length() > 1)    sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
	
	private String parseURL(String dir) {
		if (dir == null)	return null;
		String prefix = HttpServer.rootDir;
		dir = prefix + dir;
		String newDir = simplifyPath(dir);
		if (newDir.length() < prefix.length())	//should at least equals to prefix
			return null;
		String newPrefix = newDir.substring(0, prefix.length());
		if (!newPrefix.equals(prefix)) {
			return null;
		}
		System.out.println(newDir.substring(prefix.length()));
		return newDir.substring(prefix.length());
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
