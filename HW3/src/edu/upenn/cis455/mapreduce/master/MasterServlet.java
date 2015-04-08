package edu.upenn.cis455.mapreduce.master;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.infoclasses.JobInfo;
import edu.upenn.cis455.mapreduce.infoclasses.WorkerStatus;

public class MasterServlet extends HttpServlet {

	static final long serialVersionUID = 455555001;
	static final long Longest_Interval = 30 * 1000; // 30 sec
	private JobInfo job;
	private Map<String, WorkerStatus> workersStatus = new Hashtable<String, WorkerStatus>();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException {
	  
		String pathInfo = request.getPathInfo();
	  // two requests
		if ("/workerstatus".equals(pathInfo)) {
			getWorkerStatusReport(request);
		} else if ("/status".equals(pathInfo)){
			printWorkersStatus(response);
		} else {
			printResponsePage("This is a master", response);
		}
		  
	}
  
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String pathInfo = request.getPathInfo();
		if("/submitJob".equals(pathInfo)) { // Submit job
			job = getNextJobInfo(request);
		} else {
			printResponsePage("not support yet.", response);
		}
	}

	
    private void getWorkerStatusReport(HttpServletRequest request) {
    	int port;
    	long keysRead;
    	long keysWrite;
    	try {
    		port = Integer.parseInt(request.getParameter("port"));
    		keysRead = Long.parseLong(request.getParameter("keysRead"));
    		keysWrite = Long.parseLong(request.getParameter("keysWrite"));
    	} catch (Exception e) {
    		return;
    	}
    	String ip = request.getRemoteAddr();
    	String key = ip + ":" + port;
    	String status = request.getParameter("status");
    	String job = request.getParameter("job");
    	WorkerStatus workerStatus = new WorkerStatus(ip, port, job, status, keysRead, keysWrite);
    	workersStatus.put(key, workerStatus);
    }
	
    private boolean isMapPhaseComplete() {
    	boolean ret = false;
    	
    	
    	return ret;
    }
    
    private boolean isJobDone() {
    	boolean ret = false;
    	
    	return ret;
    }
    
    // generate the table of active workers' status
    private String getWorkersStatusTable() {
    	StringBuilder sb = new StringBuilder();
    	List<WorkerStatus> activeWorkers = getActiveWorkers();
    	sb.append("<p>Active jobs:</p>");
    	sb.append("<table>");
		sb.append("<tr><th>IP:port</th><th>status</th><th>job</th><th> keysRead </th><th> keysWrite </th></tr>");
		for (int i = 0; i < activeWorkers.size(); i++) {
			sb.append("<tr><td>" + activeWorkers.get(i).getIPPort() + "</td><td>" + 
					activeWorkers.get(i).getStatus() + "</td><td>" + activeWorkers.get(i).getJob() + 
					"</td><td>" + activeWorkers.get(i).getKeysRead() + 
					"</td><td>" + activeWorkers.get(i).getKeysWrite() + "</td></tr>");
		}
		sb.append("</table><br/><br/>");
    	return sb.toString();
    }
    
    
	// get submitted job information
	private JobInfo getNextJobInfo(HttpServletRequest request) {
		int mapThreads;
		int reduceThread; 
		try {
			mapThreads = Integer.parseInt(request.getParameter("mapThread"));
			reduceThread = Integer.parseInt(request.getParameter("reduceThread"));
		} catch (Exception e) {
			return null;
		}
		String name = request.getParameter("jobname");
		String inputDir = request.getParameter("inputDir");
		String outputDir = request.getParameter("outputDir");
		JobInfo job = new JobInfo(name, inputDir, outputDir, mapThreads, reduceThread);
		return job;
	}
    
    // generate the form for submitting jobs
    private String getJobSubmitForm() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<p>Submit a new job:</p>");
    	sb.append("<form action=\"submitJob\" method=\"post\">");
    	sb.append("Jobname: <input type=\"text\" name=\"jobname\"/><br/>");
    	sb.append("Input directory: <input type=\"text\" name=\"inputDir\"/><br/>");
    	sb.append("Output directory: <input type=\"text\" name=\"outputDir\"/><br/>");
    	sb.append("Num of map thread: <input type=\"number\" name=\"mapThread\" min=\"1\"/><br/>");
    	sb.append("Num of reduce thread: <input type=\"number\" name=\"reduceThread\" min=\"1\"/><br/>");
    	sb.append("<input type=\"submit\" value=\"Submit\">");
    	sb.append("</form>");
    	return sb.toString();
    }
    
    private void postRunReduce() {    
    	
    }
    
    private List<WorkerStatus> getActiveWorkers() {
    	List<WorkerStatus> ret = new ArrayList<WorkerStatus>();
    	for (WorkerStatus workerStatus : workersStatus.values()) {
    		//within 30s
    		if (workerStatus.getLastUpdated().getTime() + Longest_Interval >= System.currentTimeMillis()) {
    			ret.add(workerStatus);
    		}
    	}
    	return ret;
    }
    
	
    // print a workers' status page 
    private void printWorkersStatus(HttpServletResponse response) {
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
        writer.println("<title>Workers' status Page</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<br/>" + getWorkersStatusTable() + "<br/>");
        if (!isJobDone()) {
        	writer.println("<br/>" + getJobSubmitForm() + "<br/>");
        }
        writer.println("</body>");
        writer.println("</html>");
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
        writer.println("</body>");
        writer.println("</html>");
	}	
  
}
  
