package edu.upenn.cis455.storage;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;

public class DBWrapper {
	
	public static String envDirectory = "database";
	
	private static Environment myEnv;
	private static EntityStore store;
	
	public static void setupDirectory(String dir) {
		DBWrapper.envDirectory = dir;
		RobotInfoDA.init(dir);
		ContentDA.init(dir);
		ChannelDA.init(dir);
		UserDA.init(dir);
	}
	
}
