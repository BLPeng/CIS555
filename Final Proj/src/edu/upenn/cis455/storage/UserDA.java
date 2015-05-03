package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

// class to access User database
public class UserDA {
	public static EntityStore store;
	private static PrimaryIndex<String, User> primaryIndex;
	public static String envDirectory = "data/userDB";

	public static PrimaryIndex<String, User> getPrimaryIndex() {
		return primaryIndex;
	}

	public static void setPrimaryIndex(PrimaryIndex<String, User> primaryIndex) {
		UserDA.primaryIndex = primaryIndex;
	}

	public static void init(Environment env) {
		// absolute path from where the application was initialized.
	/*	String dir = System.getProperty("user.dir");
		File file = new File(dir, envDirectory);
		boolean noExist = file.mkdirs();
		if (noExist) {
			//
		} else {
	//		System.out.println("already created");
		}*/
	//	EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
	//	envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
//		env = new Environment(file, envConfig);
		UserDA.store = new EntityStore(env, "UserStore", storeConfig);
		primaryIndex = store.getPrimaryIndex(String.class, User.class);
		
		DatabaseShutdownHook dbShutdownHook = new DatabaseShutdownHook(env, store);
		Runtime.getRuntime().addShutdownHook(dbShutdownHook);
	}
		
	public static void putEntry(User user) {
		primaryIndex.put(user);
	}
	
	public static User getEntry(String userName) {
		return primaryIndex.get(userName);
	}
	
	public static void deleteEntry(String userName) {
		primaryIndex.delete(userName);
	}

	public static boolean containsEntry(String userName) {
		return primaryIndex.contains(userName);
	}
	public static void close() {
		if (UserDA.store != null) {
			UserDA.store.close();
		}
	}
}
