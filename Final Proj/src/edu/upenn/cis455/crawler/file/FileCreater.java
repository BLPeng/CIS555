package edu.upenn.cis455.crawler.file;

import java.io.File;
import java.io.PrintWriter;

import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.storage.Content;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.URLRelation;
import edu.upenn.cis455.storage.URLRelationDA;

public class FileCreater {
	String dir;
	String urlDir;
	String pageDir;
	File URLFile;
	File PageFile;
	final int lines = 1000;
	public FileCreater(String dir) {
		this.dir = dir;
		urlDir = "/url";
		pageDir = "/page";
		URLFile = new File(urlDir);
		PageFile = new File(pageDir);
		initStorageDirs();
		
	}
	public void setDir(String dir) {
		this.dir = dir;
		urlDir = "/url";
		pageDir = "/page";
		URLFile = new File(urlDir);
		PageFile = new File(pageDir);
		initStorageDirs();
	}
	
	public String getDir() {
		return dir;
	}
	
	public String getUrlDir() {
		return urlDir;
	}
	public void setUrlDir(String urlDir) {
		this.urlDir = urlDir;
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
	
	public void initStorageFolder(File dir) {
		clearFiles(dir);
		dir.mkdirs();
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
					writer.print(seci.getContent());
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
	private void initStorageDirs() {
		initStorageFolder(URLFile);
		initStorageFolder(PageFile);
	}
}
