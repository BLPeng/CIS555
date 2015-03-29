package edu.upenn.cis455.crawler;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.Content;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.RobotInfo;
import edu.upenn.cis455.storage.RobotInfoDA;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;



public class CrawlerWorker extends Thread{
	private final String USER_AGENT = "cis455crawler";
	private boolean run = true;
	private String dir;
	private String url;
	private int maxSize = -1;
	private int maxPage = -1;
	private Tidy mTidy = new Tidy();
	private Document document;
	private HTTPClient httpClient;
	private BlockingQueue<String> pendingURLs;
	private Set<String> fetchedURLSet;
	private boolean ifDownloaded;
	private CrawlerWorkerPool crawlerWorkerPool;
	private XPathEngineImpl xpathEngine;
	
	// crawlers share the same frontURL queue, the same fetched url set
	public CrawlerWorker(CrawlerWorkerPool crawlerWorkerPool, BlockingQueue<String> pendingURLs, Set<String> syncSet, int label) {
		super("Crawler " + String.valueOf(label));
		httpClient = new HTTPClient();
		this.pendingURLs = pendingURLs;
		this.fetchedURLSet = syncSet;
		mTidy.setForceOutput(true);
		mTidy.setPrintBodyOnly(true);
		mTidy.setXHTML(true);
		mTidy.setQuiet(true);
		mTidy.setShowWarnings(false);
		this.crawlerWorkerPool = crawlerWorkerPool;
		xpathEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine(); 
//		mTidy.setErrout(null);
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
				crawlerWorkerPool.decreaseCnt();
				this.url = pendingURLs.take();
				crawlerWorkerPool.increaseCnt();
				if (ifCrawlPage(url) && applyRobotRule(url)) {
					printProcess(url, 0);
					crawlPage(url);
				} else if (ifDownloaded) {
					printProcess(url, 1);
					crawlLocalContent(url);
				} else {
					printProcess(url, 2);
				}
			} catch (InterruptedException e) {
		//		e.printStackTrace();
				System.out.println(this.getName() + " Shutdown");
			}
		}
	}
	
	// apply robot rule to this url crawling
	private boolean applyRobotRule(String url) {
		RobotsTxtInfo robotInfo = getRobotsInfo(url);
		URL myURL;
		try {
			myURL = new URL(url);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return false;
		}
		int delay = 0;
		if (robotInfo.containsUserAgent("*") || robotInfo.containsUserAgent(USER_AGENT)) {
			String path = myURL.getPath();
			if (robotInfo.getDisallowedLinks(USER_AGENT) != null) {
				List<String> urls = robotInfo.getDisallowedLinks(USER_AGENT);
				for (String nUrl : urls) {
					if (path.startsWith(nUrl)) {
						return false;
					}
				}
			} else if (robotInfo.getDisallowedLinks(url) != null) {
				List<String> urls = robotInfo.getDisallowedLinks("*");
				for (String nUrl : urls) {
					if (path.startsWith(nUrl)) {
						return false;
					}
				}
			}
			if (robotInfo.getCrawlDelay(USER_AGENT) != null) {
				delay = robotInfo.getCrawlDelay(USER_AGENT) * 1000;
			} else if (robotInfo.getCrawlDelay("*") != null) {
				delay = robotInfo.getCrawlDelay("*") * 1000;
			}
		}
		Long lastCrawled = crawlerWorkerPool.getLastCrawledDate(myURL.getHost());
		long wait = lastCrawled + delay - System.currentTimeMillis();
		if (wait > 0) {
			try {
				crawlerWorkerPool.setLastCrawledDate(myURL.getHost(), lastCrawled + delay);
				sleep(wait);
			} catch (InterruptedException e) {
				//
			}
		} else {
			crawlerWorkerPool.setLastCrawledDate(myURL.getHost(), new Date().getTime());
		}
		return true;
	}
	
	// get local copy for url
	private void crawlLocalContent(String url) {
		boolean isXML = false;
		if (ContentDA.containsEntry(url)) {
			Content content = ContentDA.getEntry(url);
			if (content.getType().equals("xml")) {
				mTidy.setXmlTags(true);
				isXML = true;
			} else {
				mTidy.setXmlTags(false);
			}
			ByteArrayInputStream inputStream;
			try {
				inputStream = new ByteArrayInputStream(content.getContent().getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				return;
		//		e.printStackTrace();
			}
			try {
				document = mTidy.parseDOM(inputStream, null);
				if (!isXML) {	
					//	mTidy.pprint(document, System.out);
					extractURL(document, url); 
				} else {
					matchChannel(document, url);
				}
			} catch (Exception e) {
				return;
			}
		}
	}
	
	private void matchChannel(Document document, String url) {
		List<Channel> channels = ChannelDA.getEntries();
		for (Channel channel : channels) {
			String[] xpaths = channel.getXpaths();
			xpathEngine.setXPaths(xpaths);
			boolean[] ret = xpathEngine.evaluate(document);
			for (int i = 0; i < ret.length; i++) {
				if (ret[i]) {
		//			System.out.println(url + " match channel:" + channel.getName());
					ChannelDA.addXML(channel.getName(), url);
					break;
				}
			}
		}
		
	}
	// crawl page to fetch content and extract links
    public void crawlPage(String url) {
    	if (fetchedURLSet.contains(url)) {
			return;
		} else {
			fetchedURLSet.add(url);
		}
    	boolean isXML;
    	String type = "html";
		httpClient.init();
		httpClient.setURL(url);
		httpClient.setMethod("GET");
		httpClient.fetchContent();
		if ( httpClient.getResCode().startsWith("4") || httpClient.getResCode().startsWith("5")) {
			return;
		}
		Map<String, List<String>> headers = httpClient.getHeaders();
		if (!headers.containsKey("Content-Type") && !headers.containsKey("content-type")) {
			return;
		}
		String contentType;
		if (headers.containsKey("Content-Type")) {
			contentType = headers.get("Content-Type").get(0).split(";")[0];
		} else {
			contentType = headers.get("content-type").get(0).split(";")[0];
		}
		if (contentType == null) {
			return;
		} else if ("text/html".equalsIgnoreCase(contentType)) {
			isXML = false;
		} else if ("text/xml".equalsIgnoreCase(contentType)
			|| "application/xml".equalsIgnoreCase(contentType)
			|| contentType.endsWith("+xml")){
			isXML = true;
		} else { 
			return;
		}
		if (isXML) {
			mTidy.setXmlTags(true);
			type = "xml";
		} else {
			mTidy.setXmlTags(false);
			type = "html";
		}
		Content content = new Content(url, httpClient.getContent(), new Date(), type);
		ContentDA.putEntry(content);
		ByteArrayInputStream inputStream;
		try {
			inputStream = new ByteArrayInputStream(httpClient.getContent().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return;
	//		e.printStackTrace();
		}
		try {
			document = mTidy.parseDOM(inputStream, null);
			if (!isXML) {	
				//	mTidy.pprint(document, System.out);
				extractURL(document, url); 
			} else {
				matchChannel(document, url);
			}
		} catch (Exception e) {
			return;
		}
    }
    
    // to extract links from html page
    private void extractURL(Document document, String baseURI) {
    	URL baseUrl;
		try {
			baseUrl = new URL(baseURI);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return;
		}
    	NodeList elements = document.getElementsByTagName("a");
    	List<String> urls = new ArrayList<String>();
    	for (int i = 0; i < elements.getLength(); i++) {
    		Node node = elements.item(i).getAttributes().getNamedItem("href");
    		if (node == null) continue;
    		String tmp = node.getNodeValue();
    		try {
				URL url = new URL( baseUrl , tmp);
				urls.add(url.toString());
			} catch (MalformedURLException e) {
		//		e.printStackTrace();
				continue;
			}
    		
    	}
    	for (String url : urls) {
    		if (fetchedURLSet.contains(url) == false) {
    			try {
					pendingURLs.put(url);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
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
		httpClient.init();
		httpClient.setURL(url);
		httpClient.setMethod("GET");
		RobotInfo robotInfo = null;
		String content = null;
		if (RobotInfoDA.containsEntry(url)) {
			robotInfo = RobotInfoDA.getEntry(url);
			content = robotInfo.getRobotInfo();
			String date = HTTPClient.dateToString(robotInfo.getDate());
			httpClient.setRequestHeaders("If-Modified-Since", date);			
		} 
		httpClient.fetchContent();
		if ("304".equals(httpClient.getResCode())) {
			robotTxtInfo = parseRobotsContent(robotInfo.getRobotInfo());
		} else {
			crawlerWorkerPool.setLastCrawledDate(myUrl.getHost(), new Date().getTime());
			content = httpClient.getContent();
			if (content == null || httpClient.getResCode().startsWith("4")
					|| httpClient.getResCode().startsWith("5")) {
				return new RobotsTxtInfo();
			}
			robotTxtInfo = parseRobotsContent(content);
		}
		//update access time
		robotTxtInfo.setAccessedDate(new Date());
		robotInfo = new RobotInfo(url, content, new Date());
		RobotInfoDA.putEntry(robotInfo);
//		System.out.println(RobotInfoDA.containsEntry(url));
//		this.run = false;

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
			String value = pairs[1].trim();
    		if (("User-agent").equalsIgnoreCase(key)) {
    			robotInfo.addUserAgent(value);
    			curUserAgent = value;
    		} else if("Disallow".equalsIgnoreCase(key)) {
				if (curUserAgent != null) {
					robotInfo.addDisallowedLink(curUserAgent, value);
				}
			} else if("Crawl-delay".equalsIgnoreCase(key)) {
				if (curUserAgent != null) {
					int delay = 0;
					try {
						delay = Integer.valueOf(value);
					} catch (Exception e) {
						//
					} finally {
						robotInfo.addCrawlDelay(curUserAgent, delay);
					}
				}
			}
    	}
    	return robotInfo;
    }
    
	// check if given url is worth to download content
	private boolean ifCrawlPage(String url) {
		ifDownloaded = false;
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
			ifDownloaded = true;
			return false;
		} else if (httpClient.getResCode().startsWith("4")
				|| httpClient.getResCode().startsWith("5")) {
			return false;
		}
		Map<String, List<String>> headers = httpClient.getHeaders();
		if ("301".equals(httpClient.getResCode()) || "302".equals(httpClient.getResCode())) {
			fetchedURLSet.add(url);
			String newUrl = null;
			if (headers.get("Location") != null) {
				newUrl = headers.get("Location").get(0);
			} else if (headers.get("location") != null) {
				newUrl = headers.get("location").get(0);
			} else {
				return false;
			}
			if (newUrl == null) {
				return false;
			} else {
				pendingURLs.add(newUrl);
				return false;
			}
		}
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
	
	private void printProcess(String url, int type) {
		switch(type) {
		case 0: System.out.println(url + " : Downloading"); break;
		case 1: System.out.println(url + " : Not modified"); break;
		case 2: System.out.println(url + " : Not download"); break;
		}
	}

	public BlockingQueue<String> getPendingURLs() {
		return pendingURLs;
	}

	public void setPendingURLs(BlockingQueue<String> pendingURLs) {
		this.pendingURLs = pendingURLs;
	}
}
