package edu.upenn.cis455.mapreduce.worker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
						String hash = sha1(line);
						int index = getWorkerIndex(hash);
					}
				}
			} catch (InterruptedException e) {
		//		e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void shutdown() {
		this.run = false;
		this.interrupt();
	}
	
	//assume num of workers less than 65536
	private int getWorkerIndex(String hash) {
		String head = hash.substring(0, 4);
		int val = Integer.parseInt(head, 16); 
		int range = 65536 / mapThreadPool.getThreadPoolSize();
		int idx = val / range;
		return idx;
	}
	
	private String sha1(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
        byte[] result = mDigest.digest(input.getBytes("UTF-8"));
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(String.format("%02X ", result[i]));
        }         
        return sb.toString();
    }
	
}
