package edu.upenn.cis455.mapreduce.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MapThread extends Thread{
	
	private boolean run = true;
	private MapThreadPool mapThreadPool;
	private BlockingQueue<String> lines;
	
	public MapThread(MapThreadPool mapThreadPool, BlockingQueue<String> lines, int label) {
		super("MapThread " + String.valueOf(label));
		this.mapThreadPool = mapThreadPool;
		this.lines = lines;
	}

	@Override
	public void run() {
		while (run) {
			try {
				String line = lines.poll(1000, TimeUnit.MILLISECONDS);
				if (line == null && mapThreadPool.isReadComplete() && mapThreadPool.getCnt() > 0) {
					mapThreadPool.decreaseCnt();
				} else {
					if (line != null) {
						
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
