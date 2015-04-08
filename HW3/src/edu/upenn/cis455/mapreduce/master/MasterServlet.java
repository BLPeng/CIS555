package edu.upenn.cis455.mapreduce.master;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.infoclasses.WorkerStatus;

public class MasterServlet extends HttpServlet {

	static final long serialVersionUID = 455555001;
	static final long Longest_Interval = 30 * 1000; // 30 sec
	private Map<String, WorkerStatus> workerStatus = new Hashtable<>();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
	{
	  
		String path = request.getPathInfo();
	  // two requests
		if ("/workerstatus".equals(path)) {
		  
		} else if ("/status".equals(path)){
			
		} else {
			printResponsePage("This is a master", response);
		}
		  
		  

    
  }
  
	
	
    private void getWorkerStatusReport(HttpServletRequest request) {
    	String port = request.getParameter("port");
    	String ip = request.getRemoteAddr();
    	String key = ip + ":" + port;
    	String status = request.getParameter("status");
    	String job = request.getParameter("wStatusjob");
    	String keysRead = request.getParameter("keysRead");
    	String keysWrite = request.getParameter("keysWrite");
    	WorkerStatus wStatus = new WorkerStatus(ip, port, job, status, keysRead, keysWrite);
    	workerStatus.put(key, wStatus);
    }
	
    private boolean isMapPhaseComplete() {
    	return false;
    }
    
    private String getJobSubmitForm() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<form action=\"submitJob\" method=\"post\">");
    	sb.append("Jobname: <input type=\"text\" name=\"jobname\"/><br/>");
    	sb.append("Input directory: <input type=\"text\" name=\"inputDir\"/><br/>");
    	sb.append("Output directory: <input type=\"text\" name=\"outputDir\"/><br/>");
    	sb.append("Num of map thread: <input type=\"number\" name=\"mapThread\" min=\"1\"/><br/>");
    	sb.append("Num of reduce thread: <input type=\"number\" name=\"reduceThread\" min=\"1\"/><br/>");
    	sb.append("</form>");
    	return sb.toString();
    }
    
    private void postRunReduce() {    
    	
    }
    
    private List<WorkerStatus> getActiveWorkers() {
    	List<WorkerStatus> ret = new ArrayList<WorkerStatus>();
    	for (WorkerStatus workerStatus : workerStatus.values()) {
    		//within 30s
    		if (workerStatus.getLastUpdated().getTime() + Longest_Interval >= System.currentTimeMillis()) {
    			ret.add(workerStatus);
    		}
    	}
    	return ret;
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
        writer.println("<title>Master Page</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<h2> Xiaobin Chen </h2>");
        writer.println("<h2> SEAS: xiaobinc </h2>");
        writer.println("<br/>" + content + "<br/>");
        writer.println("</form>");
        writer.println("</body>");
        writer.println("</html>");
	}	
  
}
  
