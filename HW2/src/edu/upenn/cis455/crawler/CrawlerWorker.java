package edu.upenn.cis455.crawler;

import java.util.LinkedList;
import java.util.Queue;


public class CrawlerWorker extends Thread{
	
	private boolean run = true;
	private String dir;
	private String url;
	private int maxSize = -1;
	private int maxPage = -1;
	private final String USER_AGENT = "cis455crawler";
	private Queue<String> mUrlQueue = new LinkedList<String>();
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
			
		}
	}
}
