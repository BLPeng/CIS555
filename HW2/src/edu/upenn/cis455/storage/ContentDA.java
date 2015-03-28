package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

// class to access User database
public class ContentDA {
	private static EntityStore store;
	private static PrimaryIndex<String, Content> primaryIndex;
	public static String envDirectory = "data/contentDB";

	public static PrimaryIndex<String, Content> getPrimaryIndex() {
		return primaryIndex;
	}

	public static void setPrimaryIndex(PrimaryIndex<String, Content> primaryIndex) {
		ContentDA.primaryIndex = primaryIndex;
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
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
//		ContentDA.env = new Environment(file, envConfig);
		ContentDA.store = new EntityStore(env, "ContentStore", storeConfig);
		primaryIndex = store.getPrimaryIndex(String.class, Content.class);
		
		DatabaseShutdownHook dbShutdownHook = new DatabaseShutdownHook(env, store);
		Runtime.getRuntime().addShutdownHook(dbShutdownHook);
	}
		
	public static void putEntry(Content content) {
		primaryIndex.put(content);
	}
	
	public static Content getEntry(String url) {
		return primaryIndex.get(url);
	}
	
	public static void deleteEntry(String url) {
		primaryIndex.delete(url);
	}

	public static boolean containsEntry(String url) {
		return primaryIndex.contains(url);
	}

	public static void close() {
		if (ContentDA.store != null) {
			ContentDA.store.close();
		}
	}
}
