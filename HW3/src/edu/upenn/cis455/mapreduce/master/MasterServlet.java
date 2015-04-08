package edu.upenn.cis455.mapreduce.master;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.HTTPClient;
import edu.upenn.cis455.mapreduce.infoclasses.JobInfo;
import edu.upenn.cis455.mapreduce.infoclasses.WorkerStatus;


public class MasterServlet extends HttpServlet {

	static final long serialVersionUID = 455555001;
	static final long Longest_Interval = 300 * 1000; // 30 sec
	private JobInfo job;
	private Map<String, WorkerStatus> workersStatus = new Hashtable<String, WorkerStatus>();
	private HTTPClient httpClient = new HTTPClient();
	
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
			if (checkJobInfo(job, response)) {
				postRunMap(job, getActiveWorkers());
				response.sendRedirect("status");
			}
		} else if ("/test".equals(pathInfo)){
			//just for test
			getRunMapParams(request);
		} else {
			printResponsePage("not support yet.", response);
		}
	}

	private boolean checkJobInfo(JobInfo job, HttpServletResponse response) {
		boolean ret = true;
		if (job == null) {
			printResponsePage("mapThreads or reduceThreads number is empty or invalid", response);
			ret = false;
		} else if (job.getName() == null || job.getName().length() == 0) {
			printResponsePage("class name is empty", response);
			ret = false;
		} else if (job.getInputDir() == null || job.getInputDir().length() == 0) {
			printResponsePage("input directory is empty", response);
			ret = false;
		} else if (job.getOutputDir() == null || job.getOutputDir().length() == 0) {
			printResponsePage("output directory is empty", response);
			ret = false;
		} 
		return ret;
	}
	
    private void getWorkerStatusReport(HttpServletRequest request) {
    	int port;
    	long keysRead;
    	long keysWrite;
    	String ip;
    	String key;
    	String status;
    	String job;
    	try {
    		port = Integer.parseInt(URLDecoder.decode(request.getParameter("port"), "UTF-8"));
    		keysRead = Long.parseLong(URLDecoder.decode(request.getParameter("keysRead"), "UTF-8"));
    		keysWrite = Long.parseLong(URLDecoder.decode(request.getParameter("keysWrite"), "UTF-8"));
        	status = URLDecoder.decode(request.getParameter("status"), "UTF-8");
        	job = URLDecoder.decode(request.getParameter("job"), "UTF-8");
    	} catch (Exception e) {
    		return;
    	}
    	ip = request.getRemoteAddr();
    	key = ip + ":" + port;
    	WorkerStatus workerStatus = new WorkerStatus(ip, port, job, status, keysRead, keysWrite);
    	workersStatus.put(key, workerStatus);
    }
	
    // post run reduce message
    private void postRunReduce() {    
    	
    }
    
    // post run map message
    private void postRunMap(JobInfo job, List<WorkerStatus> workersStatus) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("jobName=" + job.getName());
    	sb.append("&input=" + job.getInputDir());
    	sb.append("&numThreads=" + job.getMapThreads());
    	sb.append("&numWorkers=" + workersStatus.size());
    	int count = 1;
    	for (WorkerStatus workerStatus : workersStatus) {
    		sb.append("&worker" + count + "=" + workerStatus.getIPPort());
    		count++;
    	}
    	
    	String params = sb.toString();
    	for (WorkerStatus workerStatus : workersStatus) {
    		httpClient.init();
    		httpClient.setMethod("POST");
    		httpClient.setRequestHeaders("Content-Type", "application/x-www-form-urlencoded");
    		httpClient.setURL("http://127.0.0.1:8080/master/test");		//for test
    //		httpClient.setURL("http://" + workerStatus.getIPPort() + "/runmap");
    		httpClient.setSendContent(params);
    		httpClient.connect();
    	}

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
    // for test only
    private void getRunMapParams(HttpServletRequest request) {
    	String job = request.getParameter("jobName");
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
    
	// get submitted job information
	private JobInfo getNextJobInfo(HttpServletRequest request) {
		int mapThreads;
		int reduceThread; 
		String name;
		String inputDir; 
		String outputDir;
		try {
			mapThreads = Integer.parseInt(URLDecoder.decode(request.getParameter("mapThread"), "UTF-8"));
			reduceThread = Integer.parseInt(URLDecoder.decode(request.getParameter("reduceThread"), "UTF-8"));
			inputDir = URLDecoder.decode(request.getParameter("inputDir"), "UTF-8");
			name = URLDecoder.decode(request.getParameter("jobName"), "UTF-8");
			outputDir = URLDecoder.decode(request.getParameter("outputDir"), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		JobInfo job = new JobInfo(name, inputDir, outputDir, mapThreads, reduceThread);
		return job;
	}
    
    // generate the form for submitting jobs
    private String getJobSubmitForm() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<p>Submit a new job:</p>");
    	sb.append("<form action=\"submitJob\" method=\"post\">");
    	sb.append("Job name: <input type=\"text\" name=\"jobName\"/><br/>");
    	sb.append("Input directory: <input type=\"text\" name=\"inputDir\"/><br/>");
    	sb.append("Output directory: <input type=\"text\" name=\"outputDir\"/><br/>");
    	sb.append("Num of map thread: <input type=\"number\" name=\"mapThread\" min=\"1\"/><br/>");
    	sb.append("Num of reduce thread: <input type=\"number\" name=\"reduceThread\" min=\"1\"/><br/>");
    	sb.append("<input type=\"submit\" value=\"Submit\"/>");
    	sb.append("</form>");
    	return sb.toString();
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
  
