package edu.upenn.cis455.mapreduce.worker;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.myUtil.HTTPClient;
import edu.upenn.cis455.mapreduce.myUtil.MapReduceHandler;
import edu.upenn.cis455.mapreduce.myUtil.WorkerStatus;

public class WorkerServlet extends HttpServlet implements MapReduceHandler{

	static final long serialVersionUID = 455555002;
	private static final int DURATION = 10 * 1000;
	public static final String SPOOL_IN_DIR = "spool-in";
	public static final String SPOOL_OUT_DIR = "spool-out";
	private WorkerStatus workerStatus;
	private int fileCount;
	private Object lock = new Object();;
	private Job currentJob;
	private List<String> workers;
	private HTTPClient httpClient;
	private String masterIP;
	private int masterPort;
	private String storageDir;
	private File spoolInDir;
	private Timer heartBeatTimer;
	private File spoolOutDir;
	private MapThreadPool mapThreads;
	private ReduceThreadPool reduceTreads;
	
	@Override
	public void init() throws ServletException {
	    super.init();
	    fileCount = 0;
	    httpClient = new HTTPClient();
	    storageDir = getInitParameter("storagedir");
	    mapThreads = new MapThreadPool();
	    reduceTreads = new ReduceThreadPool();
	    String[] master = getInitParameter("master").split(":");
	    String port = getInitParameter("port");
	    if (storageDir == null || master.length < 1 || master[0] == null || port == null) {
	    	throw new ServletException("wrong parameter");
	    }
	    initStorageDirs(storageDir);
	    masterIP = master[0];
	    masterPort = 80;
	    int workerPort;
	    try {
	    	if (master[1] != null) {
	    		masterPort = Integer.parseInt(master[1]);
	    	} 
	    	workerPort = Integer.parseInt(port);
	    } catch (Exception e) {
	    	throw new ServletException("wrong parameter");
	    }
	    workerStatus = new WorkerStatus("", workerPort, "None", "idle", 0, 0);
	    createHeartBeat(); 
	    
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
		printResponsePage("I am a worker", response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();
		if("/runmap".equals(path)) { 
			getRunMapParams(request);
		} else if("/pushdata".equals(path)) { 
			getPushedData(request);
		} else if("/runreduce".equals(path)) { 
			getRunReduceParams(request);
		} 
	}
	
	
	public void increaseFileCount() {
		synchronized(lock) {
			this.fileCount++;
		}
	}
	public int getFileCount() { 
		synchronized(lock) {
			this.fileCount++;
			return this.fileCount;
		}
	}
	
	private void pushData() {
		if (spoolOutDir.exists() && spoolOutDir.isDirectory()) {
			File[] files = spoolOutDir.listFiles();
			for (File file : files) {
				String fileName = file.getName();
				try {
					int idx = Integer.parseInt(fileName.substring("worker".length()));
					if (idx > 0 && idx <= workers.size()) {
						String IPPort = workers.get(idx - 1);
						String tmp = workerStatus.getIPPort();
						if (IPPort.equalsIgnoreCase(tmp)) {
							File dest = new File(spoolInDir, "worker" + getFileCount());
							InputStream input = null;
							OutputStream output = null;
							try {
								input = new FileInputStream(file);
								output = new FileOutputStream(dest);
								byte[] buf = new byte[1024];
								int bytesRead;
								while ((bytesRead = input.read(buf)) > 0) {
									output.write(buf, 0, bytesRead);
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								input.close();
								output.close();
							}
						} else {
							//assume file size not to large
							String content;
							BufferedReader br = new BufferedReader(new FileReader(file));
						    try {
						        StringBuilder sb = new StringBuilder();
						        String line = br.readLine();

						        while (line != null) {
						            sb.append(line);
						            sb.append(System.lineSeparator());
						            line = br.readLine();
						        }
						        content = sb.toString();
						    } finally {
						        br.close();
						    }
							httpClient.init();
							httpClient.setMethod("POST");
							httpClient.setSendContent(content);
							httpClient.setRequestHeaders("Content-Type", "text/html");
							httpClient.setRequestHeaders("Content-Length", String.valueOf(content.length()));
							httpClient.setURL("http://" + IPPort + "/worker/pushdata");
							httpClient.connect();
						}		
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
		
	private void getPushedData(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null) {
		    	sb.append(line);
		    	sb.append(System.lineSeparator());
		    }	
		} catch (Exception e) { /*report an error*/ }
		File file = new File(spoolInDir, "worker" + getFileCount());
		if (!file.exists()) {
			try {
				file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(sb.toString());
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}

	// init dirs
	private void initStorageDirs(String storageDir) {
		storageDir = getInitParameter("storagedir");
		spoolOutDir = new File(storageDir, SPOOL_OUT_DIR);
		spoolInDir = new File(storageDir, SPOOL_IN_DIR);
		initStorageFolder(spoolOutDir);
		initStorageFolder(spoolInDir);
	}
	
	private void createHeartBeat() {
		sendHeartBeatSignal();
		heartBeatTimer = new Timer();  
		heartBeatTimer.scheduleAtFixedRate(new HeartBeatSignal(),new Date(), DURATION);
	}
	
	
	private Job loadJob(String jobName)  {
		
		Class<?> jobClass;
		try {
			jobClass = Class.forName(jobName);
			
		} catch (ClassNotFoundException e) {
			return null;
		//	e.printStackTrace();
		}
		Job job = null;
		try {
			job = (Job) jobClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return job;
	}
	
	private void getRunReduceParams(HttpServletRequest request) throws IOException {
    	String job = request.getParameter("job");
    	String outputDir = request.getParameter("output");
    	String numThreads = request.getParameter("numThreads");
    	int numOfThreads;
    	try {
    		numOfThreads = Integer.parseInt(numThreads);
    	} catch (Exception e) {
    		numOfThreads = 0;
    	}
    	currentJob = loadJob(job);
    	if (currentJob == null) {
    		return;
    	}
    	if (outputDir == null || numOfThreads == 0) {
    		return;
    	}
    	workerStatus.setKeysRead(0);
    	workerStatus.setKeysWrite(0);
    	workerStatus.setStatus("reducing");
    	workerStatus.setIp(request.getServerName());
    	initStorageFolder(new File(storageDir, outputDir));
		this.fileCount = 0;
		reduceTreads.init(numOfThreads, storageDir, outputDir, currentJob, this);
		reduceTreads.start();
    }
	
	private void getRunMapParams(HttpServletRequest request) throws IOException {
//		String key1 = request.getParameter("key1");
    	String job = request.getParameter("job");
    	String inputDir = request.getParameter("input");
    	String numThreads = request.getParameter("numThreads");
    	String numWorkers = request.getParameter("numWorkers");
    	int numOfThreads;
    	int numOfWorkers;
    	try {
    		numOfThreads = Integer.parseInt(numThreads);
    		numOfWorkers = Integer.parseInt(numWorkers);
    	} catch (Exception e) {
    		numOfWorkers = 0;
    		numOfThreads = 0;
    	}
    	int cnt = 1;
    	workers = new ArrayList<String>();
    	for (int i = 0; i < numOfWorkers; i++) {
    		String tmp = "worker";
    		workers.add(request.getParameter(tmp + cnt));
    		cnt++;
    	} 
    	
    	currentJob = loadJob(job);
    	if (currentJob == null) {
    		return;
    	}
    	//TODO
    	if (inputDir == null || numOfThreads == 0 || numOfWorkers == 0) {
    		return;
    	}
    	workerStatus.setKeysRead(0);
    	workerStatus.setKeysWrite(0);
    	workerStatus.setStatus("mapping");
    	workerStatus.setIp(request.getServerName());
    	initStorageFolder(spoolOutDir);
		initStorageFolder(spoolInDir);
		this.fileCount = 0;
		mapThreads.init(numOfThreads, storageDir, inputDir, workers, currentJob, this);
		mapThreads.start();
    }
	// print a response page
	private void printResponsePage(String content, HttpServletResponse response) {
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();	
		} catch (IOException e) {
			return;
		//	e.printStackTrace();
		}
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Worker Page</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<h2> Xiaobin Chen </h2>");
        writer.println("<h2> SEAS: xiaobinc </h2>");
        writer.println("<br/>" + content + "<br/>");
        writer.println("</body>");
        writer.println("</html>");
	}
	
	private void sendHeartBeatSignal() {
		StringBuilder sb = new StringBuilder();
    	sb.append("?port=" + workerStatus.getPort());
    	sb.append("&status=" + workerStatus.getStatus());
    	sb.append("&job=" + workerStatus.getJob());
    	sb.append("&keysRead=" + workerStatus.getKeysRead());
    	sb.append("&keysWritten=" + workerStatus.getKeysWrite());
    	
    	String params = sb.toString();
    	httpClient.init();
		httpClient.setMethod("GET");
		httpClient.setSendContent("");
		httpClient.setRequestHeaders("Content-Type", "text/html");
		httpClient.setRequestHeaders("Content-Length", "10");
//		httpClient.setURL("http://127.0.0.1:8080/master/test");		//for test
		httpClient.setURL("http://" + masterIP + ":" + String.valueOf(masterPort)+ "/master/workerstatus" + params);
		httpClient.connect();
	}
	class HeartBeatSignal extends TimerTask {
	    public void run() {
	    	sendHeartBeatSignal();
	    }
	}
	
	// recursively delete files
	private static void clearFiles(File file) {
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
	
	public static void initStorageFolder(File dir) {
		clearFiles(dir);
		dir.mkdirs();
	}
    
	
	@Override
	public void onMapFinished() {
		workerStatus.setStatus("waiting");
		pushData();
		sendHeartBeatSignal();
	}

	@Override
	public void onKVPairRead() {
		workerStatus.increaseKeysRead();
	}

	@Override
	public void onKVPairWritten() {
		workerStatus.increaseKeysWritten();
	}

	@Override
	public void onReduceFinished() {
		workerStatus.setStatus("idle");
		sendHeartBeatSignal();
	}
	
}
  
