package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.crawler.CrawlerWorkerPool;
import edu.upenn.cis455.crawler.HTTPClient;
import edu.upenn.cis455.crawler.CrawlerWorkerPool.ThreadStats;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.URLCrawleredDA;
import edu.upenn.cis455.storage.URLQueueDA;
import edu.upenn.cis455.storage.URLRelation;
import edu.upenn.cis455.storage.URLRelationDA;



public class WorkerServlet extends ApplicationServlet{
	private List<String> workers;
	private HTTPClient httpClient;
	private String masterIP;
	private int masterPort;
	private Timer heartBeatTimer;
	private static final int DURATION = 10 * 1000;
	private String masterURL;
	String defaultDir = System.getProperty("user.dir") + "/database";
	CrawlerWorkerPool crawlerPool; 
	@Override
	  public void init() throws ServletException {
	    super.init();
	    crawlerPool = new CrawlerWorkerPool();
//	    ServletContext context = getServletContext();
//	    DBWrapper.setupDirectory(context.getInitParameter("BDBstore"));
	  }
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
	//    String tmp = System.getProperty("user.dir");
		String pathInfo = request.getPathInfo();
		if ("/master".equals(pathInfo)) {
			masterURL = request.getParameter("url");
			createHeartBeat();
		} else {
			handleCrawlerConfig(request, response);
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
		if (checkLogin(request)) {
			String pathInfo = request.getPathInfo();
			  // two requests
			if ("/worker/status".equals(pathInfo)) {
				printThreadStatus(writer, getBanner(request));	
			} else if ("/master/status".equals(pathInfo)) {
				
			} else if ("/worker/masterURL".equals(pathInfo)){
				printMasterURLSubmit(writer, "submit master url address");
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
	
	private void handleCrawlerConfig(HttpServletRequest request, HttpServletResponse response) {
		String url = request.getParameter("url");
		String dirTmp = request.getParameter("dir");
		String maxSizeS = request.getParameter("maxSize");
		String numOfFilesS = request.getParameter("numOfFiles");
		String dir = System.getProperty("user.dir") + "/database";
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
		} else if (!checkLogin(request)) {
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
			crawlerPool = new CrawlerWorkerPool();
			crawlerPool.init();
			crawlerPool.setUrl(url);
			crawlerPool.setDir(dir);
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
	
	private String getThreadStatus() {
		List<ThreadStats> status = crawlerPool.getThreadStatus();
		if (status == null) {
			return "no threads working";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>Server status</h1>");
		sb.append(System.getProperty("line.separator"));;
		sb.append("<h3>Thread pool size: " + status.size() + "</h3>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<table>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<tr>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<th>Thread    </th>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<th>Status     </th>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<th> URL    </th>");
		sb.append(System.getProperty("line.separator"));
		sb.append("</tr>");
		sb.append(System.getProperty("line.separator"));
		
		for (ThreadStats tmp : status){
			sb.append("<tr>");
			sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + tmp.threadName + "</td>");
			sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + tmp.threadStatus.toString() + "</td>");
			sb.append(System.getProperty("line.separator"));
			sb.append("<td>" + tmp.id + " " + tmp.reqUrl + "</td>");
			sb.append(System.getProperty("line.separator"));
			sb.append("</tr>");
			sb.append(System.getProperty("line.separator"));
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
    	sb.append("&status=" + getThreadStatus());
	
    	String params = sb.toString();
    	httpClient.init();
		httpClient.setMethod("GET");
		httpClient.setSendContent("");
		httpClient.setRequestHeaders("Content-Type", "text/html");
		httpClient.setRequestHeaders("Content-Length", "10");
//		httpClient.setURL("http://127.0.0.1:8080/master/test");		//for test
//		httpClient.setURL("http://" + masterIP + ":" + String.valueOf(masterPort)+ "/master/workerstatus" + params);
		httpClient.setURL(masterURL + "servlet/crawler/master/status" + params);
		httpClient.connect();
	}
	class HeartBeatSignal extends TimerTask {
	    public void run() {
	    	sendHeartBeatSignal();
	    }
	}
}
