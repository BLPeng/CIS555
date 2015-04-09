package edu.upenn.cis455.mapreduce.worker;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.myUtil.HTTPClient;
import edu.upenn.cis455.mapreduce.myUtil.WorkerStatus;

public class WorkerServlet extends HttpServlet {

	static final long serialVersionUID = 455555002;
	private static final int DURATION = 10 * 1000;
	private static final String SPOOL_IN_DIR = "spool-in";
	private static final String SPOOL_OUT_DIR = "spool-out";
	private WorkerStatus workerStatus;
	private HTTPClient httpClient;
	private String masterIP;
	private int masterPort;
	private String storageDir;
	private File spoolInDir;
	private Timer heartBeatTimer;
	private File spoolOutDir;
	private HeartBeatSignal hbs;
	
	@Override
	public void init() throws ServletException {
	    super.init();
	    httpClient = new HTTPClient();
	    storageDir = getInitParameter("storagedir");
	    String[] master = getInitParameter("master").split(":");
	    String port = getInitParameter("port");
	    if (storageDir == null || master.length < 1 || master[0] == null || port == null) {
	    	throw new ServletException("wrong parameter");
	    }
	    initStorageDirs(storageDir);
	    masterIP = master[0];
	    masterPort = 80;
	    int workerPort;
	    try {
	    	if (master[1] != null) {
	    		masterPort = Integer.parseInt(master[1]);
	    	} 
	    	workerPort = Integer.parseInt(port);
	    } catch (Exception e) {
	    	throw new ServletException("wrong parameter");
	    }
	    workerStatus = new WorkerStatus("", workerPort, "None", "running", 0, 0);
	    createHeartBeat(); 
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
		printResponsePage("I am a worker", response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();
		if("/runmap".equals(path)) { 
			getRunMapParams(request);
		} else if("/pushdata".equals(path)) { 
			
		} else if("/runreduce".equals(path)) { 
			getRunReduceParams(request);
		} 
	}
	
	// init dirs
	private void initStorageDirs(String storageDir) {
		storageDir = getInitParameter("storagedir");
		spoolOutDir = new File(storageDir, SPOOL_IN_DIR);
		spoolInDir = new File(storageDir, SPOOL_OUT_DIR);
	}
	
	private void createHeartBeat() {
		sendHeartBeatSignal();
		heartBeatTimer = new Timer();  
		heartBeatTimer.scheduleAtFixedRate(new HeartBeatSignal(),new Date(), DURATION);
	}
	
	private void getRunReduceParams(HttpServletRequest request) {
    	String job = request.getParameter("job");
    	String outputDir = request.getParameter("output");
    	String numThreads = request.getParameter("numThreads");
/*    	StringBuffer sbf = new StringBuffer();
    	String line = null;
    	try {
    	    BufferedReader reader = request.getReader();
    	    while ((line = reader.readLine()) != null) {
    	    	sbf.append(line);
    	    }	
    	} catch (Exception e) { 
    		report an error 
    		System.out.println("?");
    	}*/
    }
	
	private void getRunMapParams(HttpServletRequest request) {
//		String key1 = request.getParameter("key1");
    	String job = request.getParameter("job");
    	String inputDir = request.getParameter("input");
    	String numThreads = request.getParameter("numThreads");
    	String numWorkers = request.getParameter("numWorkers");
    	StringBuffer sbf = new StringBuffer();
    	String line = null;
    	try {
    	    BufferedReader reader = request.getReader();
    	    while ((line = reader.readLine()) != null) {
    	    	sbf.append(line);
    	    }	
    	} catch (Exception e) { 
    		/*report an error*/ 
    		System.out.println("?");
    	}

    	int numOfWorkers;
    	try {
    		numOfWorkers = Integer.parseInt(numWorkers);
    	} catch (Exception e) {
    		numOfWorkers = 0;
    	}
    	int cnt = 1;
    	List<String> workers = new ArrayList<String>();
    	for (int i = 0; i < numOfWorkers; i++) {
    		String tmp = "worker";
    		workers.add(request.getParameter(tmp + cnt));
    		cnt++;
    	} 
    }
	// print a response page
	private void printResponsePage(String content, HttpServletResponse response) {
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();	
		} catch (IOException e) {
			return;
		//	e.printStackTrace();
		}
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Worker Page</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<h2> Xiaobin Chen </h2>");
        writer.println("<h2> SEAS: xiaobinc </h2>");
        writer.println("<br/>" + content + "<br/>");
        writer.println("</body>");
        writer.println("</html>");
	}
	
	private void sendHeartBeatSignal() {
		StringBuilder sb = new StringBuilder();
    	sb.append("?port=" + workerStatus.getPort());
    	sb.append("&status=" + workerStatus.getStatus());
    	sb.append("&job=" + workerStatus.getJob());
    	sb.append("&keysRead=" + workerStatus.getKeysRead());
    	sb.append("&keysWritten=" + workerStatus.getKeysWrite());
    	
    	String params = sb.toString();
    	httpClient.init();
		httpClient.setMethod("GET");
		httpClient.setSendContent("");
		httpClient.setRequestHeaders("Content-Type", "text/html");
		httpClient.setRequestHeaders("Content-Length", "10");
//		httpClient.setURL("http://127.0.0.1:8080/master/test");		//for test
		httpClient.setURL("http://" + masterIP + ":" + String.valueOf(masterPort)+ "/master/workerstatus" + params);
		httpClient.connect();
	}
	class HeartBeatSignal extends TimerTask {
	    public void run() {
	    	sendHeartBeatSignal();
	    }
	}
}
  
