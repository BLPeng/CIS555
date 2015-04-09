package edu.upenn.cis455.mapreduce.myUtil;

public class JobInfo {
	private String name;
	private String inputDir;
	private String outputDir;
	private int mapThreads;
	private int reduceThread;
	public JobInfo() {
		
	}
	public JobInfo(String name, String inputDir, String outputDir, int mapThreads, int reduceThreads) {
		this.name = name;
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.mapThreads = mapThreads;
		this.reduceThread = reduceThreads;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInputDir() {
		return inputDir;
	}
	public void setInputDir(String inputDir) {
		this.inputDir = inputDir;
	}
	public String getOutputDir() {
		return outputDir;
	}
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	public int getMapThreads() {
		return mapThreads;
	}
	public void setMapThreads(int mapThreads) {
		this.mapThreads = mapThreads;
	}
	public int getReduceThread() {
		return reduceThread;
	}
	public void setReduceThread(int reduceThread) {
		this.reduceThread = reduceThread;
	}
}
