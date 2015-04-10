package edu.upenn.cis455.mapreduce.worker;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.worker.ReduceThreadPool.KVPair;

public class ReduceThread extends Thread{
	
	private boolean run = true;
	private ReduceThreadPool reduceThreadPool;
	private BlockingQueue<KVPair> lines;
	private Job currentJob;
	private Context context;
	
	public ReduceThread(ReduceThreadPool reduceThreadPool, BlockingQueue<KVPair> lines, int label, Job currentJob, Context context) {
		super("ReduceThread " + String.valueOf(label));
		this.reduceThreadPool = reduceThreadPool;
		this.lines = lines;
		this.context = context;
		this.currentJob = currentJob;
	}

	@Override
	public void run() {
		while (run) {
			try {
				KVPair kv = lines.poll(1000, TimeUnit.MILLISECONDS);
				if (kv == null && reduceThreadPool.isReduceComplete() && lines.size() == 0) {
					reduceThreadPool.decreaseCnt();
					continue;
				}
				if (kv == null) {
					continue;
				}
				List<String> values = kv.values;
				if (values == null ) {
					continue;
				} else {
					if (values != null) {
						String[] values1 = new String[kv.values.size()];
						values.toArray(values1);
						currentJob.reduce(kv.key, values1, context);
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
