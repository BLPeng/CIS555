package edu.upenn.cis455.storage;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;

public class DBWrapper {
	
	private static String envDirectory = null;
	
	private static Environment myEnv;
	private static EntityStore store;
	
	public static void setupDirectory(String dir) {
		DBWrapper.envDirectory = dir;
		UserDA.init(dir);
		ChannelDA.init(dir);
		ContentDA.init(dir);
		RobotInfoDA.init(dir);
	}
	
}
