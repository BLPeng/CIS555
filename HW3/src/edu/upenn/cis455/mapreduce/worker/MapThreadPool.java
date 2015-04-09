package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.myUtil.MaperHandler;
import edu.upenn.cis455.mapreduce.myUtil.MyUtils;



public class MapThreadPool {
	private int threadPoolSize = 10;
	private int queueSize = 40960;
	private MaperHandler handler;
	private List<String> workers;
	private File inputFile;
	private String inputDir;
	private String storage;
	private MapThread[] pools;
	private Job currentJob;
	private int workerSize;
	private boolean readCompleted = false;
	private boolean init = false;
	private int workingThread = threadPoolSize;
	private MapContext context;
	private final Object lock = new Object();
	private BlockingQueue<KVPair> lines = new ArrayBlockingQueue<KVPair>(queueSize);
	
	public class KVPair {
		String key;
		String value;
		public KVPair(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}
	
	public MapThreadPool() {
		
	}
	
	public void init (int poolSize, String storege, String inputDir, List<String> workers, Job currentJob, MaperHandler hander) throws IOException {
		this.inputDir = inputDir;
		this.storage = storege;
		this.handler = hander;
		this.workers = workers;
		this.workerSize = workers.size();
		this.threadPoolSize = poolSize >= 0 ? poolSize : 0;
		this.workingThread = threadPoolSize;
		this.currentJob = currentJob;
		pools = new MapThread[threadPoolSize];
		readCompleted = false;
		lines.clear();
		inputFile = null;
		context = new MapContext(storage, workerSize);
		init = true;
	}
	
	public void start() {
		if (init == false) {
			return;
		}
		if (threadPoolSize <= 0 || inputDir == null || storage == null) {
			return;
		}
		inputFile = new File(storage, inputDir);
		if(!inputFile.exists() || !inputFile.isDirectory()) {
			return;
		}
		
		for (int i = 0; i < threadPoolSize; i++){
			pools[i] = new MapThread(this, lines, i + 1, currentJob, context);
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
	
	private void pushData() {
		
	}
	
	

    public void decreaseCnt() throws InterruptedException {
        synchronized (lock) {
        	this.workingThread--;
        	if (this.workingThread <= 0) {
        		shutdown();
        		onMapFinish(); 
        		if (handler != null) {
        			handler.onMapFinished();
        		}
        	}
        }
    }
    
 // read KV pair from input files
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
    
    // read KV pair from input file
	private void readFile(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			try {
				while((line = reader.readLine()) != null) {
					String[] parts = line.split("\\t");
					if (parts.length < 2) {
						continue;
					}
					handler.onKVPairRead();
					lines.put(new KVPair(parts[0].trim(), parts[1].trim()));
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
	
	class MapContext implements Context {

		private PrintWriter[] writers = null;
		private int workerSize;
		
		//setup spool-out files;
		public MapContext(String storage, int workerSize) throws IOException {
			this.workerSize = workerSize;
			writers = new PrintWriter[workerSize];
			File spoolOutDir = new File(storage, WorkerServlet.SPOOL_OUT_DIR);
			for(int i = 0; i < workerSize; i++) {
				String fileName = "worker" + (i + 1);
				File file = new File(spoolOutDir, fileName);
				file.createNewFile();
				writers[i] = new PrintWriter(new FileWriter(file, true), true);
			}
		}
		
		@Override
		public void write(String key, String value) {
			String hash;
			try {				
/*				int index1 = MyUtils.getWorkerIndex("3ffff", 4);
				index1 = MyUtils.getWorkerIndex("7ffff", 4);
				index1 = MyUtils.getWorkerIndex("bffff", 4);
				index1 = MyUtils.getWorkerIndex("fffff", 4);*/
				hash = MyUtils.sha1(key);
				int index = MyUtils.getWorkerIndex(hash, workerSize);
				PrintWriter writer = writers[index];
				if(writer != null) {
					synchronized (writer) {
						writer.print(key);
						writer.print('\t');
						writer.print(value);
						writer.println();
					}
					handler.onKVPairWritten();
				}
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
					
		}
		
		public void closeFiles() {
			if(writers != null) {
				for(PrintWriter w : writers) {
					if (w != null) {
						w.close();
					}
				}
			}
		}
	
	}
}
