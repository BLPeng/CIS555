package edu.upenn.cis.cis455.webserver;

import org.apache.log4j.Logger;


class HttpServer {
	
	private static int portNumber;
	private static String rootDir;
	static final Logger logger = Logger.getLogger(HttpServer.class.getName());
	
	public HttpServer(int port, String rootDir){
		portNumber = port;
		rootDir = new String(rootDir);
	}
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
	
    public static void main(String args[])
    {
    	logger.info("Starting HttpServer.");
		if (!validateInput(args))	return;
        runServer();
    }
    
    private static boolean validateInput(String[] args){
		
		//validate inputs
		if (args == null || args.length != 2){	
			logger.error("Number of arguments is wrong!");
			return false;
		}
		
		//validate port number
		int portNum = Integer.valueOf(args[0]);
		if (portNum < 0 || portNum >= 65536){
			logger.error("Port Number is invalid!");
			System.out.println("Valid port number range:[0, 65535], [1024, 65535] is recommended!");
			return false;
		}
		portNumber = portNum;
		rootDir = args[1];
		System.out.println("Server Name: Xiaobin Chen. PennKey: xiaobinc");
        System.out.println("Port: " + portNumber + "\rRoot Directory: " + rootDir);
		return true;
		//validate root dir
		

	}
	
	private static void runServer(){
		
	}
  
}
