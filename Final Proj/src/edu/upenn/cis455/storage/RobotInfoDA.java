package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

// class to access User database
public class RobotInfoDA {
	private static EntityStore store;
	private static PrimaryIndex<String, RobotInfo> primaryIndex;
	public static String envDirectory = "data/robotDB";

	public static PrimaryIndex<String, RobotInfo> getPrimaryIndex() {
		return primaryIndex;
	}

	public static void setPrimaryIndex(PrimaryIndex<String, RobotInfo> primaryIndex) {
		RobotInfoDA.primaryIndex = primaryIndex;
	}

	public static void init(Environment env)  {
		// absolute path from where the application was initialized.
		String dir = System.getProperty("user.dir");
		File file = new File(dir, DBWrapper.envDirectory);
		boolean noExist = file.mkdirs();
		if (noExist) {
			//
		} else {
	//		System.out.println("already created");
		}
	//	EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
	//	envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
//		env = new Environment(file, envConfig);
		RobotInfoDA.store = new EntityStore(env, "RobotStore", storeConfig);
		primaryIndex = store.getPrimaryIndex(String.class, RobotInfo.class);
		
		DatabaseShutdownHook dbShutdownHook = new DatabaseShutdownHook(env, store);
		Runtime.getRuntime().addShutdownHook(dbShutdownHook);
	}
		
	public static void putEntry(RobotInfo robotInfo) {
		primaryIndex.put(robotInfo);
	}
	
	public static RobotInfo getEntry(String userName) {
		return primaryIndex.get(userName);
	}
	
	public static void deleteEntry(String userName) {
		primaryIndex.delete(userName);
	}
	
	public static boolean containsEntry(String userName) {
		return primaryIndex.contains(userName);
	}

	public static void close() {
		if (RobotInfoDA.store != null) {
			try {
				RobotInfoDA.store.close();
			} catch (Exception e) {
				
			}
			
		}
	}

}
