package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.crawler.CrawlerWorkerPool;
import edu.upenn.cis455.crawler.CrawlerWorkerPool.ThreadStats;
import edu.upenn.cis455.crawler.file.FileCreater;
import edu.upenn.cis455.crawler.info.HTTPClient;
import edu.upenn.cis455.crawler.info.WorkerInfos;
import edu.upenn.cis455.crawler.info.WorkerStatus;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.URLCrawleredDA;
import edu.upenn.cis455.storage.URLQueueDA;
import edu.upenn.cis455.storage.URLRelation;
import edu.upenn.cis455.storage.URLRelationDA;





public class CrawlerServlet extends ApplicationServlet{
	
	private HTTPClient httpClient;
	private final long Longest_Interval = 10 * 1000; // 30 sec
	private WorkerInfos workerInfos;
	private Timer heartBeatTimer;
	private FileCreater fileCreater;
	private static final int DURATION = 10 * 1000;
	private String masterURL;
	String defaultDir = System.getProperty("user.dir") + "/database";
	CrawlerWorkerPool crawlerPool; 
	@Override
	  public void init() throws ServletException {
	    super.init();
	    workerInfos = new WorkerInfos();
	    workerInfos.setPort(getInitParameter("port"));
//	    defaultDir = System.getProperty("user.dir") + "/database/" + workerInfos.getPort();
	    defaultDir = "/database/";
	    crawlerPool = new CrawlerWorkerPool(workerInfos);
	    crawlerPool.setDir(defaultDir);
	    fileCreater = new FileCreater(crawlerPool.getDir());
	    DBWrapper.setupDirectory(crawlerPool.getDir());
	    httpClient = new HTTPClient();
//	    ServletContext context = getServletContext();
//	    DBWrapper.setupDirectory(context.getInitParameter("BDBstore"));
	  }
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
	//    String tmp = System.getProperty("user.dir");
		String pathInfo = request.getPathInfo();
		if ("/worker/masterURL".equals(pathInfo)) {
			masterURL = request.getParameter("url");
			createHeartBeat();
		} else if ("/master/submitURLS".equals(pathInfo)) {
			startAllWorkers(request, response);
		} else if ("/worker/pushdata".equals(pathInfo)) {
			getPushedData(request);
		} else {
			handleCrawlerConfig(request, response);
		}
	}

	private void getPushedData(HttpServletRequest request) {
		String line = null;
		try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null) {
		    	URLQueueDA.pushURL(line);
		    }	
		} catch (Exception e) { /*report an error*/ }
	}
	private void stopAllWorkers(HttpServletRequest request,
			HttpServletResponse response) {
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		List<String> addrs = new ArrayList<String>(workerInfos.workersStatus.keySet());
		for (String addr : addrs) {
			httpClient.init();
			httpClient.setMethod("GET");
			httpClient.setSendContent("");
			httpClient.setRequestHeaders("Content-Type", "text/html");
			httpClient.setRequestHeaders("Content-Length", "10");
//			httpClient.setURL("http://127.0.0.1:8080/master/test");		//for test
//			httpClient.setURL("http://" + masterIP + ":" + String.valueOf(masterPort)+ "/master/workerstatus" + params);
			httpClient.setURL("http://" + addr + "/servlet/crawler/worker/stop");
			httpClient.connect();
		}
		
	}
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}     
//		if (checkLogin(request)) {
		if (true) {
			String pathInfo = request.getPathInfo();
			  // two requests
			if ("/worker/status".equals(pathInfo)) {
				printThreadStatus(writer, getBanner(request));	
			} else if ("/master/status".equals(pathInfo)) {
				getWorkersStatus(writer);
			} else if ("/master/updateWorkerLists".equals(pathInfo)) {
				updateWorkerLists(writer);
			} else if ("/master/workerHB".equals(pathInfo)){
				updateWorkersStatus(request);
				updateWorkerLists(writer);
			} else if ("/master/stop".equals(pathInfo)) {
				stopAllWorkers(request, response);
			} else if ("/worker/masterURL".equals(pathInfo)){
				printMasterURLSubmit(writer, "submit master url address");
			} else if ("/worker/getFiles".equals(pathInfo)){
				if (crawlerPool != null) {
					crawlerPool.shutdown();
				}
				DBWrapper.setupDirectory(crawlerPool.getDir());
				fileCreater.createURLFiles();
				fileCreater.createPageFiles();
				DBWrapper.closeDBs();
			}else if ("/worker/urlFeed".equals(pathInfo)){
				getUrlFeed(request);
			} else if ("/worker/updateWorkerLists".equals(pathInfo)) {
				updateWorkerLists1(request);
			} else if ("/worker/new".equals(pathInfo)){
				printLoginPage(writer, getBanner(request), null);
				try {
					response.sendRedirect("status");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ("/worker/stop".equals(pathInfo)){
				if (crawlerPool != null) {
					crawlerPool.shutdown();
				}
				try {
					response.sendRedirect("status");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ("/worker/start".equals(pathInfo)){
				if (crawlerPool != null) {
					crawlerPool.shutdown();
				}
				DBWrapper.setupDirectory(crawlerPool.getDir());
//				URLQueueDA.clear();
//				URLCrawleredDA.clear();
				crawlerPool.init();
				crawlerPool.start();
				try {
					response.sendRedirect("status");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ("/worker/clear".equals(pathInfo)){
				if (crawlerPool != null) {
					crawlerPool.shutdown();
				}
				DBWrapper.setupDirectory(crawlerPool.getDir());
				URLQueueDA.clear();
				URLCrawleredDA.clear();
				DBWrapper.closeDBs();
				try {
					response.sendRedirect("status");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ("/worker/pageRankData".equals(pathInfo)){
				if (crawlerPool.getDir() == null) {
					DBWrapper.setupDirectory(defaultDir);
				} else {
					DBWrapper.setupDirectory(crawlerPool.getDir());
				}
				
				List<URLRelation> lists = URLRelationDA.getEntries();
				printURLRelation1(writer, lists, response);
				DBWrapper.closeDBs();
			}	
		} else {
			try {
				response.sendRedirect("/servlet/login");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	private void updateWorkerLists1(HttpServletRequest request) {
		String numWorkers = request.getParameter("numWorkers");
    	int numOfWorkers;
    	try {
    		numOfWorkers = Integer.parseInt(numWorkers);
    	} catch (Exception e) {
    		numOfWorkers = 0;
    	}
    	int cnt = 1;
    	Hashtable<String, WorkerStatus> newWorkersStatus1 = new Hashtable<String, WorkerStatus>();
    	for (int i = 0; i < numOfWorkers; i++) {
    		String tmp = "worker";
    		String key = request.getParameter(tmp + cnt);
    		String[] params = key.split(":");
    		try {
    			int port = Integer.valueOf(params[1]);
    			newWorkersStatus1.put(key, new WorkerStatus(params[0], port, ""));
    		} catch (Exception e) {
    			continue;
    		}
    		cnt++;
    	} 
    	workerInfos.workersStatus1 = newWorkersStatus1;
	}
	
	private void updateWorkerLists(PrintWriter writer) {
		StringBuilder sb = new StringBuilder();
		sb.append("?numWorkers=" + workerInfos.workersStatus.size());
		int count = 1;
		for (String addr : workerInfos.workersStatus.keySet()) {
			sb.append("&worker" + count + "=" + addr);
			count++;
		}
		String params = sb.toString();
		List<String> addrs = new ArrayList<String>(workerInfos.workersStatus.keySet());
		for (String url : addrs) {
			httpClient.init();
			httpClient.setMethod("GET");
			httpClient.setSendContent("");
			httpClient.setRequestHeaders("Content-Type", "text/html");
			httpClient.setRequestHeaders("Content-Length", "10");
//			httpClient.setURL("http://127.0.0.1:8080/master/test");		//for test
//			httpClient.setURL("http://" + masterIP + ":" + String.valueOf(masterPort)+ "/master/workerstatus" + params);
			httpClient.setURL("http://" + url + "/servlet/crawler/worker/updateWorkerLists" + params);
			httpClient.connect();
		}
	}
	
	private void getUrlFeed(HttpServletRequest request) {
		String url = request.getParameter("URL");
		if (url == null) {
			return;
		}
		try {
			url = URLDecoder.decode(url, "UTF-8");
			URLQueueDA.pushURL(url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			return;
		}
	}
	
	private void startAllWorkers(HttpServletRequest request,
			HttpServletResponse response) {
		String seeds = request.getParameter("seeds");
		String[] urls = seeds.split(";");
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		if (urls == null || urls.length == 0 ) {
			printErrorPage(writer, getBanner(request), "empty seed url");
		}
		int i = 0;
		int size = workerInfos.workersStatus.size();
		if (size == 0) {
			return;
		}
		List<String> addrs = new ArrayList<String>(workerInfos.workersStatus.keySet());
		for (String url : urls) {
			String addr = addrs.get(i % size);
			String params = null;
			try {
				params = "?URL="+URLEncoder.encode(url, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			}
			httpClient.init();
			httpClient.setMethod("GET");
			httpClient.setSendContent("");
			httpClient.setRequestHeaders("Content-Type", "text/html");
			httpClient.setRequestHeaders("Content-Length", "10");
//			httpClient.setURL("http://127.0.0.1:8080/master/test");		//for test
//			httpClient.setURL("http://" + masterIP + ":" + String.valueOf(masterPort)+ "/master/workerstatus" + params);
			httpClient.setURL("http://" + addr + "/servlet/crawler/worker/urlFeed" + params);
			httpClient.connect();
			i++;
		}
		for (String addr : addrs) {
			httpClient.init();
			httpClient.setMethod("GET");
			httpClient.setSendContent("");
			httpClient.setRequestHeaders("Content-Type", "text/html");
			httpClient.setRequestHeaders("Content-Length", "10");
//			httpClient.setURL("http://127.0.0.1:8080/master/test");		//for test
//			httpClient.setURL("http://" + masterIP + ":" + String.valueOf(masterPort)+ "/master/workerstatus" + params);
			httpClient.setURL("http://" + addr + "/servlet/crawler/worker/start");
			httpClient.connect();
		}
	}
	
	private void updateWorkersStatus(HttpServletRequest request) {
		int port;
    	String status = "";
    	String ip;
    	String key;
    	try {
    		port = Integer.parseInt(URLDecoder.decode(request.getParameter("port"), "UTF-8"));
      //  	status = URLDecoder.decode(request.getParameter("status"), "UTF-8");
    	} catch (Exception e) {
    		return;
    	}
    	ip = request.getRemoteAddr();
    	key = ip + ":" + port;
    	WorkerStatus workerStatus = new WorkerStatus(ip, port, status);
    	Hashtable<String, WorkerStatus> newWorkersStatus = new Hashtable<>();
    	workerInfos.workersStatus.put(key, workerStatus);
		for (String tmp : workerInfos.workersStatus.keySet()) {
			if (workerInfos.workersStatus.get(tmp).getLastUpdated().getTime() + Longest_Interval > System.currentTimeMillis()) {
				newWorkersStatus.put(tmp, workerInfos.workersStatus.get(tmp));
			}
		}
		workerInfos.workersStatus = newWorkersStatus;
	}
	
	private void handleCrawlerConfig(HttpServletRequest request, HttpServletResponse response) {
		String url = request.getParameter("url");
		String dirTmp = request.getParameter("dir");
		String maxSizeS = request.getParameter("maxSize");
		String numOfFilesS = request.getParameter("numOfFiles");
		String dir = defaultDir;
		int maxSize = 1;
		int numOfFiles = -1;
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
	//		url = URLDecoder.decode(url, "utf-8").trim();
			dirTmp = URLDecoder.decode(dirTmp, "utf-8").trim();
			maxSizeS = URLDecoder.decode(maxSizeS, "utf-8").trim();
			numOfFilesS = URLDecoder.decode(numOfFilesS, "utf-8").trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		if (url == null || url.length() == 0 ) {
			printErrorPage(writer, getBanner(request), "empty seed url");
		} else if (false) {
			try {
				response.sendRedirect("/servlet/login");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (dirTmp != null && dirTmp.length() != 0) {
				dir = System.getProperty("user.dir") + "/" + dirTmp;
			} else {
				
			}
			try {
				maxSize = Integer.parseInt(maxSizeS);
			} catch (NumberFormatException e) {
		//		System.err.println("Invalid number " + maxSizeS);
			}
			try {
				numOfFiles = Integer.parseInt(numOfFilesS);
			} catch (NumberFormatException e) {
		//		System.err.println("Invalid number " + maxSizeS);
			}
			if (crawlerPool != null) {
				crawlerPool.shutdown();
			}
			DBWrapper.setupDirectory(dir);
//			URLQueueDA.clear();
//			URLCrawleredDA.clear();
			crawlerPool = new CrawlerWorkerPool(workerInfos);
			crawlerPool.init();
			crawlerPool.setUrl(url);
			crawlerPool.setDir(dir);
			fileCreater.setDir(crawlerPool.getDir());
			crawlerPool.setMaxSize(maxSize);
			crawlerPool.setMaxPage(numOfFiles);
			crawlerPool.start();
			try {
				response.sendRedirect("status");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void printURLRelation1(PrintWriter writer, List<URLRelation> lists, HttpServletResponse response) {
        if (lists != null) {
        	response.setHeader("Content-Type", "text/plain");
            response.setHeader("success", "yes");
            response.setHeader("number-of-lines", String.valueOf(lists.size()));
            for (URLRelation url : lists) {
            	writer.print(url.getUrl());
            	writer.print("\t");
            	for (String outURL : url.getUrls()) {
            		writer.print(outURL);
                	writer.print(" ");
            	}
            	writer.print(System.lineSeparator());
            }
        }
		writer.close();		
	}
	
	private void printURLRelation(PrintWriter writer, List<URLRelation> lists) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>URL relations</title>");
        writer.println("</head>");
        writer.println("<body>");
        if (lists != null) {
        	writer.print(lists.size());
        	writer.print(System.lineSeparator());
        	writer.print("</br>");
            for (URLRelation url : lists) {
            	writer.print(url.getUrl());
            	writer.print(" ");
            	for (String outURL : url.getUrls()) {
            		writer.print(outURL);
                	writer.print(" ");
            	}
            	writer.print(System.lineSeparator());
            	writer.print("</br>");
            }
        }
        writer.println("</body>");
        writer.println("</html>");
		writer.close();		
	}
	
	private void printThreadStatus(PrintWriter writer, String banner) {
		writer.write(getControlPage());
		writer.close();	
	}
	
	private void printMasterURLSubmit(PrintWriter writer, String msg) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Crawler Page</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println(msg+"<br/>");
        writer.println("Master Page!<br/>");
		writer.println("<h2>Workers status</h2>");
		if (workerInfos.workersStatus1 == null) {
			return;
		}
		for (String key : workerInfos.workersStatus1.keySet()) {
			String url = "<a href=\"Http://" + key+"/servlet/crawler/worker/status\">" + key + "</a>";
			writer.println("<h4>" + url + "</h4>");
			writer.println("<h5>" + workerInfos.workersStatus1.get(key).getStatus() + "</h5>");
		}
        writer.println("<form method=\"post\">");
        writer.println("MasterURL: <input type=\"text\" name=\"url\"><br>");
        writer.println("<input type=\"submit\" value=\"Submit\">");
        writer.println("</form>");
        writer.println("</body>");
        writer.println("</html>");
		writer.close();	
	}
	
	private void printLoginPage(PrintWriter writer, String banner, String msg) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Crawler Page</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println(banner+"<br/>");
        writer.println(msg+"<br/>");
        writer.println("Crawler Page!<br/>");
        writer.println("<form method=\"post\">");
        writer.println("Seed: <input type=\"text\" name=\"url\"><br>");
        writer.println("Dir: <input type=\"password\" name=\"dir\"><br>");
        writer.println("MaxSize: <input type=\"text\" name=\"maxSize\"><br>");
        writer.println("MaxDocNum: <input type=\"password\" name=\"numOfFiles\"><br>");
        writer.println("<input type=\"submit\" value=\"crawler\">");
        writer.println("</form>");
        writer.println("</body>");
        writer.println("</html>");
		writer.close();	
	}
	
	private void getWorkersStatus(PrintWriter writer) {
//		StringBuilder sb = new StringBuilder();
		writer.println("<html>");
		writer.println("<head>");
		writer.println("<title>Crawler Page</title>");
		writer.println("</head>");
		writer.println("<body>");
		writer.println("<h2>Workers status</h2>");
		if (workerInfos.workersStatus == null) {
			return;
		}
		for (String key : workerInfos.workersStatus.keySet()) {
			String url = "<a href=\"Http://" + key+"/servlet/crawler/worker/status\">" + key + "</a>";
			writer.println("<h4>" + url + "</h4>");
			writer.println("<h5>" + workerInfos.workersStatus.get(key).getStatus() + "</h5>");
		}
		  // generate the form for submitting jobs
		writer.println("<p>Submit url seeds:</p>");
		writer.println("<form action=\"submitURLS\" method=\"post\">");
		writer.println("Seeds: <input type=\"text\" name=\"seeds\" size=\"100\"/><br/>");
		writer.println("<input type=\"submit\" value=\"Submit\" />");
		writer.println("</form>");
		writer.println("<a href=\"stop\">");
		writer.println("<button>Stop</button></a>");
		writer.println("</body>");
        writer.println("</html>");
	}
	
	
	private String getThreadStatus() {
		List<ThreadStats> status = crawlerPool.getThreadStatus();
		if (status == null) {
			return "no threads working";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>Crawler status</h1>");
	//	sb.append(System.getProperty("line.separator"));;
		sb.append("<h3>Thread pool size: " + status.size() + "</h3>");
	//	sb.append(System.getProperty("line.separator"));
		sb.append("<table>");
	//	sb.append(System.getProperty("line.separator"));
		sb.append("<tr>");
	//	sb.append(System.getProperty("line.separator"));
		sb.append("<th>Thread    </th>");
	//	sb.append(System.getProperty("line.separator"));
		sb.append("<th>Status     </th>");
	//	sb.append(System.getProperty("line.separator"));
		sb.append("<th> URL    </th>");
	//	sb.append(System.getProperty("line.separator"));
		sb.append("</tr>");
	//	sb.append(System.getProperty("line.separator"));
		
		for (ThreadStats tmp : status){
			sb.append("<tr>");
	//		sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + tmp.threadName + "</td>");
	//		sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + tmp.threadStatus.toString() + "</td>");
	//		sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + tmp.id + " " + tmp.reqUrl + "</td>");
	//		sb.append(System.getProperty("line.separator"));
			sb.append("</tr>");
	//		sb.append(System.getProperty("line.separator"));
		}	
		sb.append("</table>");
		return sb.toString();
	}
	
	private String getControlPage() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>Crawler Page</title>");
		sb.append("</head>");
		sb.append("<body>");
		if (crawlerPool == null) {
			return "<h1>Server status: no crawling</h1>";
		}		
		sb.append(getThreadStatus());
		sb.append(System.getProperty("line.separator"));
		sb.append("<a href=\"new\">");
		sb.append("<button>New</button></a>");
		sb.append("<a href=\"masterURL\">");
		sb.append("<button>Master</button></a>");
		sb.append("<a href=\"stop\">");
		sb.append("<button>Stop</button></a>");
		sb.append("<a href=\"start\">");
		sb.append("<button>Start</button></a>");
		sb.append("<a href=\"clear\">");
		sb.append("<button>Clear Queue</button></a>");
		sb.append("<a href=\"getFiles\">");
		sb.append("<button>Generate Files</button></a>");
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
	}
	
	private void createHeartBeat() {
		sendHeartBeatSignal();
		heartBeatTimer = new Timer();  
		heartBeatTimer.scheduleAtFixedRate(new HeartBeatSignal(),new Date(), DURATION);
	}
	
	private void sendHeartBeatSignal() {
		StringBuilder sb = new StringBuilder();
		sb.append("?port=" + workerInfos.port);
    	String params = sb.toString();
    	httpClient.init();
		httpClient.setMethod("GET");
		httpClient.setSendContent("");
		httpClient.setRequestHeaders("Content-Type", "text/html");
		httpClient.setRequestHeaders("Content-Length", "10");
//		httpClient.setURL("http://127.0.0.1:8080/master/test");		//for test
//		httpClient.setURL("http://" + masterIP + ":" + String.valueOf(masterPort)+ "/master/workerstatus" + params);
		httpClient.setURL(masterURL + "/servlet/crawler/master/workerHB" + params);
		httpClient.connect();
	}
	class HeartBeatSignal extends TimerTask {
	    public void run() {
	    	sendHeartBeatSignal();
	    }
	}
}
