package edu.upenn.cis455.crawler;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.upenn.cis455.crawler.info.WorkerInfos;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.URLQ;
import edu.upenn.cis455.storage.URLQueueDA;
import edu.upenn.cis455.storage.URLVisited;
import edu.upenn.cis455.storage.URLVisitedDA;


public class CrawlerWorkerPool {
	private int threadPoolSize = 5;	//for multi-processor /core, increase this number
	private int queueSize = 409600;
	private WorkerInfos workerInfo;
	private CrawlerWorker[] pools;
	private String dir;
	private String url;
	private int maxSize = 10;
	private int maxPage = -1;
	private int curPage = 0;
	private int workingThread = threadPoolSize;
	private final Object lock = new Object();
	private Set<String> syncSet = Collections.newSetFromMap(new Hashtable<String, Boolean>());
	private BlockingQueue<String> pendingURLs = new ArrayBlockingQueue<String>(queueSize);
//	private Hashtable<String, Long> lastCrawled = new Hashtable<String, Long>();
	
	public CrawlerWorkerPool(WorkerInfos workerInfo) { 		
		this.workerInfo = workerInfo;
		pools = new CrawlerWorker[threadPoolSize];
		init();
	}
	
	public  List<String> getWorkers() {
		return new ArrayList<String>(workerInfo.workersStatus1.keySet());
	}
	
	public int getPort() {
		return workerInfo.port;
	}
	
	public void init() {
		for (int i = 0; i < threadPoolSize; i++){
			pools[i] = new CrawlerWorker(this, pendingURLs, syncSet, i + 1);
		}
	}
	public void start() {	
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].setDir(dir);
			pools[i].setMaxPage(maxPage);
			pools[i].setMaxSize(maxSize);
			pools[i].start();
		}		
	}
	
	public List<ThreadStats> getThreadStatus() {
		if (threadPoolSize != pools.length)	return new ArrayList<ThreadStats>();
		List<ThreadStats> status = new ArrayList<ThreadStats>();
		for (int i = 0; i < threadPoolSize; i++){
			URLQ url = pools[i].getCrawlURL();
			String tmpURL = null;
			long id = 0;
			if (url != null) {
				tmpURL = url.getUrl();
				id = url.getId();
			}
			ThreadStats ts = new ThreadStats(pools[i].getName(), pools[i].getState(), tmpURL, id);
			status.add(ts);
		}
		return status;
	}

	public void shutdown1() {
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].shutdown();
		}
	}
	
	public void shutdown() {
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].shutdown();
		}
		DBWrapper.closeDBs();
	}
	
    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		try {
			this.pendingURLs.put(url);
			URLQueueDA.pushURL(url);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public class ThreadStats {
    	public String threadName;
    	public Thread.State threadStatus;
    	public String reqUrl;
    	public long id;
    	public ThreadStats(String name, State status, String url, Long id) {
    		this.threadName = name;
    		this.threadStatus = status;
    		this.reqUrl = url;
    		this.id = id;
    	}
    }
	
	public void increaseCnt() throws InterruptedException {
        synchronized (lock) {
            this.workingThread++;
        }
    }

    public void decreaseCnt() throws InterruptedException {
        synchronized (lock) {
        	this.workingThread--;
        	this.curPage++;
        	if (this.curPage > this.maxPage && this.maxPage > 0) {
        		if (this.workingThread > 0) {
        			lock.wait();
        		} else {
        		//	shutdown();
        			return;
        		}
        		
        	}
        	if (this.workingThread == 0 && URLQueueDA.size() == 0) {
   //     		shutdown();
        	}
/*        	if (this.workingThread == 0 && this.pendingURLs.size() == 0) {
        		shutdown();
        	}*/
        }
    }
    
    public long getLastCrawledDate(String url) {
//    	Long time = lastCrawled.get(url);
    	URLVisited tmp = URLVisitedDA.getEntry(url);
    	if (tmp == null) {
    		return 0;
    	}
    	Long time = tmp.getTime();
    	if (time == null) {
    		return 0;
    	} else {
    		return time;
    	}
    }
    
    public void setLastCrawledDate(String url, long l) {
    	URLVisitedDA.putEntry(new URLVisited(l, url));
  //  	lastCrawled.put(url, l);
    }

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}
	
	
}
