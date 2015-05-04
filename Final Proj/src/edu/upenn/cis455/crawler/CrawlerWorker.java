package edu.upenn.cis455.crawler;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
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

import edu.upenn.cis455.crawler.info.MyUtils;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.Content;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.RobotInfo;
import edu.upenn.cis455.storage.RobotInfoDA;
import edu.upenn.cis455.storage.URLCrawleredDA;
import edu.upenn.cis455.storage.URLQ;
import edu.upenn.cis455.storage.URLQueueDA;
import edu.upenn.cis455.storage.URLRelation;
import edu.upenn.cis455.storage.URLRelationDA;
import edu.upenn.cis455.storage.URLVisited;
import edu.upenn.cis455.storage.URLVisitedDA;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;



public class CrawlerWorker extends Thread{
	private final String USER_AGENT = "cis455crawler";

	private boolean run = true;
	private String dir;
	private String url;
	private URLQ crawlURL;
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
		mTidy.setShowErrors(0);
		mTidy.setShowWarnings(false);
//		mTidy.setErrout(null);     // error why???
		this.crawlerWorkerPool = crawlerWorkerPool;
		xpathEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine(); 
//		mTidy.setErrout(null);
	}

	public URLQ getCrawlURL() {
		return crawlURL;
	}

	public void setCrawlURL(URLQ crawlURL) {
		this.crawlURL = crawlURL;
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
		this.run = true;
		while (run) {
			try {
				crawlerWorkerPool.decreaseCnt();
	//			this.url = pendingURLs.take();
				crawlURL = URLQueueDA.pollURL();
				crawlerWorkerPool.increaseCnt();
				if (crawlURL == null) {
					continue;
				}
				this.url = crawlURL.getUrl();	
				if (ifCrawlPage(url) && applyRobotRule(url)) {
					printProcess(crawlURL, 0);
					crawlPage(url);
				} else if (ifDownloaded) {
					printProcess(crawlURL, 1);
					crawlLocalContent(url);
				} else {
					printProcess(crawlURL, 2);
				}
			} catch (InterruptedException e) {
		//		e.printStackTrace();
				
			}
		}
		System.out.println(this.getName() + " Shutdown");
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
	//				matchChannel(document, url);
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
/*    	if (fetchedURLSet.contains(url)) {
			return;
		} else {
			fetchedURLSet.add(url);
		}*/
    	if (URLCrawleredDA.containsEntry(url)) {
    		return;
    	} else {
    		URLCrawleredDA.putEntry(new URLVisited((long) 0, url));
    	}
    	boolean isXML;
    	String type = "html";
		httpClient.init();
		httpClient.setURL(url);
		httpClient.setMethod("GET");
		httpClient.connect();
		if ( httpClient.getResCode() == null || httpClient.getResCode().startsWith("4") || httpClient.getResCode().startsWith("5")) {
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
//		System.out.println(httpClient.getContent());
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
	//			matchChannel(document, url);
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
    	String[] urls1 = new String[urls.size()];
    	urls1 = urls.toArray(urls1);
    	URLRelationDA.putEntry(new URLRelation(baseURI, urls1));
    	for (String url : urls) {
    		if (URLVisitedDA.containsEntry(url) == false) {
    			try {
					String hash = MyUtils.sha1(url);
//					int index = MyUtils.getWorkerIndex(hash, workerSize);
					URLQueueDA.pushURL(url);
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
					URLQueueDA.pushURL(url);
				} 
	//						
//			pendingURLs.put(url);
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
		httpClient.connect();
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
		if (url == null) {
			return false;
		}
		if (URLVisitedDA.containsEntry(url)) {
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
		httpClient.connect();
	    if ("304".equals(httpClient.getResCode())) {
			ifDownloaded = true;
			return false;
		} else if (httpClient.getResCode() == null || (httpClient.getResCode().startsWith("4")
				|| httpClient.getResCode().startsWith("5"))) {
			return false;
		}
		Map<String, List<String>> headers = httpClient.getHeaders();
		if ("301".equals(httpClient.getResCode()) || "302".equals(httpClient.getResCode())) {
//			fetchedURLSet.add(url);
			URLCrawleredDA.putEntry(new URLVisited((long) 0, url));
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
				URLQueueDA.pushURL(newUrl);
//				pendingURLs.add(newUrl);
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
	
	private void printProcess(URLQ url, int type) {
		switch(type) {
		case 0: System.out.println(url.getId() + " " + url.getUrl() + " : Downloading"); break;
		case 1: System.out.println(url.getId() + " " + url.getUrl() + " : Not modified"); break;
		case 2: System.out.println(url.getId() + " " + url.getUrl() + " : Not download"); break;
		}
	}

	public BlockingQueue<String> getPendingURLs() {
		return pendingURLs;
	}

	public void setPendingURLs(BlockingQueue<String> pendingURLs) {
		this.pendingURLs = pendingURLs;
	}
}
