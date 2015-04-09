package edu.upenn.cis455.mapreduce.worker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.myUtil.MyUtils;
import edu.upenn.cis455.mapreduce.worker.MapThreadPool.KVPair;

public class MapThread extends Thread{
	
	private boolean run = true;
	private MapThreadPool mapThreadPool;
	private BlockingQueue<KVPair> lines;
	private Job currentJob;
	private Context context;
	
	public MapThread(MapThreadPool mapThreadPool, BlockingQueue<KVPair> lines, int label, Job currentJob, Context context) {
		super("MapThread " + String.valueOf(label));
		this.mapThreadPool = mapThreadPool;
		this.lines = lines;
		this.context = context;
		this.currentJob = currentJob;
	}

	@Override
	public void run() {
		while (run) {
			try {
				KVPair kv = lines.poll(1000, TimeUnit.MILLISECONDS);
				String line = kv.value;
				if (line == null && mapThreadPool.isReadComplete() && mapThreadPool.getCnt() > 0) {
					mapThreadPool.decreaseCnt();
				} else {
					if (line != null) {
						currentJob.map(kv.key, kv.value, context);
					}
				}
			} catch (InterruptedException e) {
		//		e.printStackTrace();
			}
		}
	}
	
	public void shutdown() {
		this.run = false;
		this.interrupt();
	}

	
}
