package edu.upenn.cis455.mapreduce.worker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

public class WorkerServlet extends HttpServlet {

	static final long serialVersionUID = 455555002;

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
//			handlePushData(request, response);
		} else if("/runreduce".equals(path)) { 
//			handleRunReduce(request, response);
		} 
	}
	
	
	private void getRunMapParams(HttpServletRequest request) {
		String key1 = request.getParameter("key1");
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
}
  
