package edu.upenn.cis455.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.servlet.HTTPClient;
import edu.upenn.cis455.storage.Content;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.RobotInfo;
import edu.upenn.cis455.storage.RobotInfoDA;



public class CrawlerWorker extends Thread{
	private final String USER_AGENT = "cis455crawler";
	private boolean run = true;
	private String dir;
	private String url;
	private int maxSize = -1;
	private int maxPage = -1;
	private HTTPClient httpClient;
	private BlockingQueue<String> pendingURLs;
	private Set<String> fetchedURLSet;
	
	// crawlers share the same frontURL queue, the same fetched url set
	public CrawlerWorker(BlockingQueue<String> pendingURLs, Set<String> syncSet, int label) {
		super("Crawler " + String.valueOf(label));
		httpClient = new HTTPClient();
		this.pendingURLs = pendingURLs;
		this.fetchedURLSet = syncSet;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public void setMaxPage(int maxPage) {
		this.maxPage = maxPage;
	}
	
	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}
	@Override
	public void run() {
		while (run) {
			try {
				this.url = pendingURLs.take();
				if (ifCrawlPage(url) && applyRobotRule(url)) {
					fetchedURLSet.add(url);
					crawlPage(url);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean applyRobotRule(String url) {
		RobotsTxtInfo robotInfo = getRobotsInfo(url);
		int delay = 0;
		if (robotInfo.containsUserAgent("*") || robotInfo.containsUserAgent(USER_AGENT)) {
			if (robotInfo.getDisallowedLinks(url) != null) {
				return false;
			}
			if (robotInfo.getCrawlDelay("*") != null) {
				delay = robotInfo.getCrawlDelay("*") * 1000;
			} else if (robotInfo.getCrawlDelay(USER_AGENT) != null) {
				delay = robotInfo.getCrawlDelay(USER_AGENT) * 1000;
			}
		}
		long wait = robotInfo.getAccessedDate().getTime() + delay - System.currentTimeMillis();
		if (wait > 0) {
			try {
				sleep(wait);
			} catch (InterruptedException e) {
				//
			}
		}
		return true;
	}
	
    private String crawlPage(String url) {
    	
    	return null;
    }
	// fetch robot.txt info
    private RobotsTxtInfo getRobotsInfo(String url) {
    	URL myUrl;
    	RobotsTxtInfo robotTxtInfo = null;
		try {
			myUrl = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new RobotsTxtInfo();
		}
		url = myUrl.getProtocol() + "://" + myUrl.getHost() + "/robots.txt";
		if (RobotInfoDA.containsEntry(url)) {
			RobotInfo robotInfo = RobotInfoDA.getEntry(url);
			//update access time
			robotTxtInfo = parseRobotsContent(robotInfo.getRobotInfo());
			robotTxtInfo.setAccessedDate(robotInfo.getDate());
			robotInfo.setDate(new Date());
			RobotInfoDA.putEntry(robotInfo);
		} else {
			httpClient.init();
			httpClient.setURL(url);
			httpClient.setMethod("GET");
			httpClient.fetchContent();
			String content = httpClient.getContent();
			if (content == null) {
				return new RobotsTxtInfo();
			}
			robotTxtInfo = parseRobotsContent(content);
			robotTxtInfo.setAccessedDate(new Date());
			RobotInfo robotInfo = new RobotInfo(url, content, new Date());
			RobotInfoDA.putEntry(robotInfo);
			System.out.println(RobotInfoDA.containsEntry(url));
			this.run = false;
		}
		return robotTxtInfo;
    }
    
    // parse robot.txt to robotTxtInfo object
    private RobotsTxtInfo parseRobotsContent(String content) {
    	RobotsTxtInfo robotInfo = new RobotsTxtInfo();
    	if (content == null) {
    		return null;
    	}
    	String[] lines = content.split(System.lineSeparator());
    	String curUserAgent = null;
    	for (String line : lines) {
    		//skip not relevant lines
    		if (line.length() == 0 || line.startsWith("#")) {
    			curUserAgent = null;
    			continue;
    		}
    		String[] pairs = line.split(":");
    		if (pairs.length != 2) {
    			curUserAgent = null;
    			continue;
    		}
    		String key = pairs[0].trim();
			String value = pairs[1].trim().toLowerCase();
    		if (("User-agent").equalsIgnoreCase(key)) {
    			robotInfo.addUserAgent(value);
    			curUserAgent = value;
    		} else if("Disallow".equalsIgnoreCase(key)) {
				if (curUserAgent != null) {
					robotInfo.addDisallowedLink(curUserAgent, value);
				}
			} else if("Crawl-delay".equalsIgnoreCase(key)) {
				if (curUserAgent != null) {
					robotInfo.addCrawlDelay(curUserAgent, Integer.valueOf(value));
				}
			}
    	}
    	return robotInfo;
    }
    
	// check if given url is worth to download content
	private boolean ifCrawlPage(String url) {
		if (fetchedURLSet.contains(url)) {
			return false;
		}
		httpClient.init();
		httpClient.setURL(url);
		httpClient.setMethod("HEAD");
		if (ContentDA.containsEntry(url)) {
			Content content = ContentDA.getEntry(url);
			String date = HTTPClient.dateToString(content.getLastAccessedAt());
			httpClient.setRequestHeaders("If-Modified-Since", date);
		}
		httpClient.fetchContent();
		if ("304".equals(httpClient.getResCode())) {
			return false;
		}
		Map<String, List<String>> headers = httpClient.getHeaders();
		if (headers.containsKey("Content-Length")) {
			int len = Integer.valueOf(headers.get("Content-Length").get(0));
			if (len > this.maxSize * 1024 * 1024) {
				return false;
			}
		}
		if (!headers.containsKey("Content-Type") && !headers.containsKey("content-type")) {
			return false;
		}
		String contentType;
		if (headers.containsKey("Content-Type")) {
			contentType = headers.get("Content-Type").get(0).split(";")[0];
		} else {
			contentType = headers.get("content-type").get(0).split(";")[0];
		}
		if (contentType == null) {
			return false;
		} else if ("text/html".equalsIgnoreCase(contentType) 
			|| "text/xml".equalsIgnoreCase(contentType)
			|| "application/xml".equalsIgnoreCase(contentType)
			|| contentType.endsWith("+xml")){
			return true;
		} else { 
			return false;
		}
	}
	
	public void shutdown() {
		this.run = false;
		this.interrupt();
	}
}
