package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.crawler.CrawlerWorkerPool;
import edu.upenn.cis455.crawler.CrawlerWorkerPool.ThreadStats;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.URLCrawleredDA;
import edu.upenn.cis455.storage.URLQueueDA;



public class CrawlerServlet extends ApplicationServlet{
	
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
				response.sendRedirect("login");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (dirTmp != null) {
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
			if ("/status".equals(pathInfo)) {
				printThreadStatus(writer, getBanner(request));	
			} else if ("/new".equals(pathInfo)){
				printLoginPage(writer, getBanner(request), null);
				try {
					response.sendRedirect("status");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ("/stop".equals(pathInfo)){
				if (crawlerPool != null) {
					crawlerPool.shutdown();
				}
				try {
					response.sendRedirect("status");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ("/start".equals(pathInfo)){
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
			}	
			else if ("/clear".equals(pathInfo)){
				if (crawlerPool != null) {
					crawlerPool.shutdown();
				}
				DBWrapper.setupDirectory(crawlerPool.getDir());
				URLQueueDA.clear();
				URLCrawleredDA.clear();
				crawlerPool.init();
				crawlerPool.start();
				try {
					response.sendRedirect("status");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		} else {
			try {
				response.sendRedirect("login");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void printThreadStatus(PrintWriter writer, String banner) {
		writer.write(getControlPage());
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
		List<ThreadStats> status = crawlerPool.getThreadStatus();
		if (crawlerPool == null)	return "";
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
		sb.append(System.getProperty("line.separator"));
		sb.append("<a href=\"new\">");
		sb.append("<button>New</button></a>");
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
}
