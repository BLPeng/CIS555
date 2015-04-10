package edu.upenn.cis455.mapreduce.myUtil;

public interface MapReduceHandler {

	public void onMapFinished();
	public void onKVPairRead();
	public void onKVPairWritten();
	public void onReduceFinished();
}
