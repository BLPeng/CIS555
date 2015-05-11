package edu.upenn.cis455.crawler.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.storage.Content;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.URLCrawleredDA;
import edu.upenn.cis455.storage.URLRelation;
import edu.upenn.cis455.storage.URLRelationDA;
import edu.upenn.cis455.storage.URLVisited;

public class FileCreater {
	String dir;
	static String urlDir = "/url";
	static File URLFile = new File(urlDir);
	String pageDir = "/page";
	File PageFile;
	final static int lines = 1000;
	private static Object lock = new Object();
	public FileCreater(String dir) {
		this.dir = dir;
		PageFile = new File(pageDir);
		initStorageDirs(false);
		
	}
	public void setDir(String dir, boolean clear) {
		this.dir = dir;
		urlDir = "/url";
		PageFile = new File(pageDir);
		initStorageDirs(clear);
	}
	
	public String getDir() {
		return dir;
	}
	
	public static String getUrlDir() {
		return urlDir;
	}
	public static void setUrlDir(String urldir) {
		urlDir = urldir;
	}
	
	public String getPageDir() {
		return pageDir;
	}
	
	public void setPageDir(String pageDir) {
		this.pageDir = pageDir;
	}
	
	// recursively delete files
	private void clearFiles(File file) {
		if(file == null || !file.exists()) {
			return;
		}
		if(file.isDirectory()) { 
			File[] files = file.listFiles();
			for(File f : files) {
				clearFiles(f);
			}
		}
		file.delete();
	}
	
	public void initStorageFolder(File dir, boolean clear) {
		if (clear) {
			clearFiles(dir);
		}
		dir.mkdirs();
	}
	
	public static void createURLFile(URLRelation urlRelation) {
		if (urlRelation == null) {
			return;
		}
		synchronized(lock) {
			long i = URLCrawleredDA.getCount();
			long cnt = i / lines;
			File file = new File(URLFile, "url" + cnt);
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
				out.print(urlRelation.getUrl());
				out.print("\t");
				out.print("0");
				out.print("\t");
				out.print("1");
				out.print("\t");
				for (String url : urlRelation.getUrls()) {
					out.print(url + " ");
				}
				out.print(System.lineSeparator());
				
			}catch (IOException e) {
				    //exception handling left as an exercise for the reader
			}finally {
		    	URLCrawleredDA.putEntry(new URLVisited((long) 0, urlRelation.getUrl()));
			}
		}
	}
	
	public void createURLFiles() {
		EntityCursor<URLRelation> cursor = URLRelationDA.getCursor();
		int i = 0;
		try {
			File dest; 
			PrintWriter writer = null;
		    for (URLRelation seci : cursor) {	    	
				try {	
					if (i % lines == 0) {
			    		dest = new File(URLFile, "url" + i);
			    		writer = new PrintWriter(dest, "UTF-8");
			    	}	
					writer.print(seci.getUrl());
					writer.print("\t");
					writer.print("0");
					writer.print("\t");
					writer.print("1");
					writer.print("\t");
					for (String url : seci.getUrls()) {
						writer.print(url + " ");
					}
					writer.print(System.lineSeparator());
					i++;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (i % lines == 0) {
						writer.close();
					}
				}
		    	
		    }
		// Always make sure the cursor is closed when we are done with it.
		} finally {
			cursor.close();
		}
	}
	
	public void createPageFiles(String url, String content) {
		long i = ContentDA.getCount();
		File dest; 
		PrintWriter writer = null;	    	
		try {	
		    dest = new File(PageFile, "page" + i);
		    writer = new PrintWriter(dest, "UTF-8");
		    writer.println("<!--" + url + "-->");
			writer.print(content);
			writer.print(System.lineSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}
	
	public void createPageFiles() {
		EntityCursor<Content> cursor = ContentDA.getCursor();
		int i = 0;
		try {
			File dest; 
			PrintWriter writer = null;
		    for (Content seci : cursor) {	    	
				try {	
			    	dest = new File(PageFile, "page" + i);
			    	writer = new PrintWriter(dest, "UTF-8");
			    	writer.println("<!--" + seci.getUrl() + "-->");
					writer.print("nothing, use new method!");
					writer.print(System.lineSeparator());
					i++;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (i % 100 == 0) {
						writer.close();
					}
				}
		    	
		    }
		// Always make sure the cursor is closed when we are done with it.
		} finally {
			cursor.close();
		}
	}
	
	// init dirs
	private void initStorageDirs(boolean clear) {
		initStorageFolder(URLFile, clear);
		initStorageFolder(PageFile, clear);
	}
}
