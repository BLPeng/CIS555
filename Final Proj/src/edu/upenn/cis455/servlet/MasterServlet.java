package edu.upenn.cis455.servlet;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.*;



public class MasterServlet extends ApplicationServlet {

	static final long serialVersionUID = 455555001;
	static final long Longest_Interval = 300 * 1000; // 30 sec
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException {
	  
		String pathInfo = request.getPathInfo();
	  // two requests
		if ("/workerstatus".equals(pathInfo)) {
			
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

	
		} else if ("/test".equals(pathInfo)){

		} else {
			printResponsePage("not support yet.", response);
		}
	}

	
  
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
        writer.println("<br/>" + content + "<br/>");
        writer.println("</body>");
        writer.println("</html>");
	}	
  
}
  
