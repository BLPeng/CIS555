package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;



public class MapThreadPool {
	private int threadPoolSize = 10;
	private int queueSize = 40960;
	private File inputFile;
	private String inputDir;
	private String storage;
	private MapThread[] pools;
	private boolean readCompleted = false;
	private int workingThread = threadPoolSize;
	private final Object lock = new Object();
	private BlockingQueue<String> lines = new ArrayBlockingQueue<String>(queueSize);
	
	public MapThreadPool() {
		
	}
	
	public void init (int poolSize, String storege, String inputDir) {
		this.inputDir = inputDir;
		this.storage = storege;
		this.threadPoolSize = poolSize >= 0 ? poolSize : 0;
		this.workingThread = threadPoolSize;
		pools = new MapThread[threadPoolSize];
		readCompleted = false;
		lines.clear();
		inputFile = null;
	}
	
	public void start() {
		
		if (threadPoolSize <= 0 || inputDir == null || storage == null) {
			return;
		}
		inputFile = new File(storage, inputDir);
		if(!inputFile.exists() || !inputFile.isDirectory()) {
			return;
		}
		
		for (int i = 0; i < threadPoolSize; i++){
			pools[i] = new MapThread(this, lines, i + 1);
		}
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].start();
		}
		readFiles(inputFile);
	}

	public void shutdown() {
		for (int i = 0; i < threadPoolSize; i++){
			pools[i].shutdown();
		}
	}

	public boolean isReadComplete() {
		return readCompleted;
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
	
	public int getCnt() throws InterruptedException {
        synchronized (lock) {
            return this.workingThread;
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
        	if (this.workingThread <= 0) {
        		shutdown();
        	}
        }
    }
    
    
    private void readFiles(File fileDir) {
    	
		if(!fileDir.exists() || !fileDir.isDirectory()) {
			return;
		}
		File[] files = fileDir.listFiles();
		for(File file : files) {
			if(file.isFile()) {
				readFile(file);
			} else if(file.isDirectory()) {
				readFiles(file);
			}
		}
		readCompleted = true;
    }
    
	private void readFile(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			try {
				while((line = reader.readLine()) != null) {
					String[] parts = line.split("\\s");
					if (parts.length < 2) {
						continue;
					}
					lines.put(parts[1].trim());
				}
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}

    }
    
	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}
}
