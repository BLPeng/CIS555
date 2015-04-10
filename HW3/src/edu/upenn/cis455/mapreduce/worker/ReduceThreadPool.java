package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.myUtil.MapReduceHandler;
import edu.upenn.cis455.mapreduce.worker.MapThreadPool.KVPair;


public class ReduceThreadPool {
	private int threadPoolSize = 10;
	private int queueSize = 40960;
	private MapReduceHandler handler;
	private File inputFile;
	private String outputDir;
	private String storage;
	private ReduceThread[] pools;
	private Job currentJob;
	private boolean reduceComplete = false;
	private boolean init = false;
	private int workingThread = threadPoolSize;
	private ReduceContext context;
	private final Object lock = new Object();
	private BlockingQueue<KVPair> lines = new ArrayBlockingQueue<KVPair>(queueSize);
	
	public class KVPair {
		String key;
		List<String> values;
		public KVPair(String key, List<String> values) {
			this.key = key;
			this.values = values;
		}
	}
	
	public ReduceThreadPool() {
		
	}
	
	public void init (int poolSize, String storege, String outputDir, Job currentJob, MapReduceHandler hander) throws IOException {
		this.outputDir = outputDir;
		this.storage = storege;
		this.handler = hander;
		this.threadPoolSize = poolSize >= 0 ? poolSize : 0;
		this.workingThread = threadPoolSize;
		this.currentJob = currentJob;
		pools = new ReduceThread[threadPoolSize];
		reduceComplete = false;
		lines.clear();
		inputFile = null;
		context = new ReduceContext(storage);
		init = true;
	}
	
	public void start() {
		if (init == false) {
			return;
		}
		if (threadPoolSize <= 0 || outputDir == null || storage == null) {
			return;
		}
		inputFile = new File(storage, WorkerServlet.SPOOL_IN_DIR);
		if(!inputFile.exists() || !inputFile.isDirectory()) {
			return;
		}
		
		for (int i = 0; i < threadPoolSize; i++){
			pools[i] = new ReduceThread(this, lines, i + 1, currentJob, context);
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

	public boolean isReduceComplete() {
		return reduceComplete;
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
	
	private void onMapFinish() {
		context.closeFiles();
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
        		onMapFinish(); 
        		if (handler != null) {
        			handler.onReduceFinished();
        		}
        	}
        }
    }
    
 // read KV pair from input files
    private void readFiles(File fileDir) {
    	
		if(!fileDir.exists() || !fileDir.isDirectory()) {
			return;
		}
		BufferedReader reader = null;
		String command = "cat ";
		File[] files = fileDir.listFiles();
		for(File file : files) {
			command += file.getAbsolutePath();
			command += " ";
		}
		command += " | sort";
		String[] cmd = {
				"sh",
				"-c",
				command
				};
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			String lastKey = null;
			List<String> values = new ArrayList<String>();
			while((line = reader.readLine()) != null) {
				String[] parts = line.split("\\t");
				if (parts.length < 2) {
					continue;
				}
				String thisKey = parts[0].trim();
				String value = parts[1].trim();
				handler.onKVPairRead();
				if (lastKey != null && !thisKey.equals(lastKey)) {
					lines.add(new KVPair(lastKey, new ArrayList<String>(values)));
					values.clear();
				}
				lastKey = thisKey;
				values.add(value);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reduceComplete = true;
    }
    

    
	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}
	
	class ReduceContext implements Context {

		private PrintWriter writer = null;

		
		//setup spool-out files;
		public ReduceContext(String storage) throws IOException {

			File outputFile = new File(storage, outputDir);
			String fileName = "result";
			File file = new File(outputFile, fileName);
			if(file.exists()) {
				file.delete();
			}
			try {
				file.createNewFile();
				writer = new PrintWriter(new FileWriter(file, true), true);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		@Override
		public void write(String key, String value) {
			String hash;
			if(writer != null) {
				synchronized (writer) {
					writer.print(key);
					writer.print('\t');
					writer.print(value);
					writer.println();
				}
				handler.onKVPairWritten();
			}
					
		}
		
		public void closeFiles() {
			if (writer != null) {
				writer.close();
			}
		}
	
	}
}
