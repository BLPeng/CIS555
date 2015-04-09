package edu.upenn.cis455.mapreduce.myUtil;

public interface MaperHandler {

	public void onMapFinished();
	public void onKVPairRead();
	public void onKVPairWritten();

}
