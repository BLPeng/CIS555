package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {
	
	public static String envDirectory = "database";
	
	public static Environment myEnv;
	private static EntityStore store;
	
	public static void setupDirectory(String dir) {
		DBWrapper.envDirectory = dir;
		String basedir = System.getProperty("user.dir");
		File file = new File(dir);
		boolean noExist = file.mkdirs();
		if (noExist) {
			//
		} else {
	//		System.out.println("already created");
		}
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		envConfig.setTransactional(true);
		Environment env = new Environment(file, envConfig);
		myEnv = env;
		RobotInfoDA.init(env);
		long id = ContentDA.init(env);
		URLQueueDA.init(env, id);
		ChannelDA.init(env);
		UserDA.init(env);
		URLVisitedDA.init(env);
		URLCrawleredDA.init(env);
		URLRelationDA.init(env);
	}
	
	public static void closeDBs() {
		RobotInfoDA.close();
		ContentDA.close();
		ChannelDA.close();
		UserDA.close();
		URLQueueDA.close();
		URLVisitedDA.close();
		URLCrawleredDA.close();
		URLRelationDA.close();
		if (DBWrapper.myEnv != null) {
			DBWrapper.myEnv.close();
		}	
	}
	
}
