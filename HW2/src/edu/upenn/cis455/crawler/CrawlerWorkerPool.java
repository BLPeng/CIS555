package edu.upenn.cis455.crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.upenn.cis455.storage.DBWrapper;

public class CrawlerWorkerPool {
	private final int threadPoolSize = 10;	//for multi-processor /core, increase this number
	private final int queueSize = 409600;
	private CrawlerWorker[] pools;
	private String dir;
	private String url;
	private int maxSize = -1;
	private int maxPage = -1;
	private int curPage = 0;
	private int workingThread = threadPoolSize;
	private final Object lock = new Object();
	private Set<String> syncSet = Collections.newSetFromMap(new Hashtable<String, Boolean>());
	private BlockingQueue<String> pendingURLs = new ArrayBlockingQueue<String>(queueSize);
	private Hashtable<String, Long> lastCrawled = new Hashtable<String, Long>();
	
	public CrawlerWorkerPool() { 		
		pools = new CrawlerWorker[threadPoolSize];
	}
	
	public void start() {
		for (int i = 0; i < threadPoolSize; i++){
			pools[i] = new CrawlerWorker(this, pendingURLs, syncSet, i + 1);
			pools[i].setDir(dir);
			pools[i].setMaxPage(maxPage);
			pools[i].setMaxSize(maxSize);
		}
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].start();
		}		
	}
	
	public List<ThreadStats> getThreadStatus() {
		if (threadPoolSize != pools.length)	return null;
		List<ThreadStats> status = new ArrayList<ThreadStats>();
		for (int i = 0; i < threadPoolSize; i++){
			ThreadStats ts = new ThreadStats(pools[i].getName(), pools[i].getState(), pools[i].getUrl());
			status.add(ts);
		}
		return status;
	}

	public void shutdown() {
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].shutdown();
		}
	//	DBWrapper.closeDBs();
	}
	
    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		try {
			this.pendingURLs.put(url);
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
    	String threadName;
    	Thread.State threadStatus;
    	String reqUrl;
    	public ThreadStats(String name, Thread.State status, String url) {
    		this.threadName = name;
    		this.threadStatus = status;
    		this.reqUrl = url;
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
        			shutdown();
        		}
        		
        	}
        	if (this.workingThread == 0 && this.pendingURLs.size() == 0) {
        		shutdown();
        	}
        }
    }
    
    public long getLastCrawledDate(String url) {
    	Long time = lastCrawled.get(url);
    	if (time == null) {
    		return 0;
    	} else {
    		return time;
    	}
    }
    
    public void setLastCrawledDate(String url, long l) {
    	lastCrawled.put(url, l);
    }
}
