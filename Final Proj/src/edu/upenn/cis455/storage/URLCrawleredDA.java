package edu.upenn.cis455.storage;


import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

// class to access User database
public class URLCrawleredDA {
	public static EntityStore store;
	private static PrimaryIndex<String, URLVisited> primaryIndex;
	public static String envDirectory = "data/channelDB";
	private static EntityCursor<URLVisited> cursor;
	
	public static PrimaryIndex<String, URLVisited> getPrimaryIndex() {
		return primaryIndex;
	}

	public static void setPrimaryIndex(PrimaryIndex<String, URLVisited> primaryIndex) {
		URLCrawleredDA.primaryIndex = primaryIndex;
	}

	public static void init(Environment env)  {

	//	EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
	//	envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		storeConfig.setTransactional(false);
		storeConfig.setDeferredWrite(true);
	//	Environment env = new Environment(file, envConfig);
	//	DBWrapper.myEnv = env;
		URLCrawleredDA.store = new EntityStore(env, "URLCrawledStore", storeConfig);
		primaryIndex = store.getPrimaryIndex(String.class, URLVisited.class);
		DatabaseShutdownHook dbShutdownHook = new DatabaseShutdownHook(env, store);
		Runtime.getRuntime().addShutdownHook(dbShutdownHook);
	}
		
	public static void putEntry(URLVisited url) {
			primaryIndex.put(url);
	}
	
	public static int size() {
		if (primaryIndex == null) {
			return 0;
		}
		return (int) primaryIndex.count();
	}
	
	public static boolean containsEntry(String url) {
		boolean ret = primaryIndex.contains(url);
		return ret;
	}
	
	public static URLVisited getEntry(String url) {
		return primaryIndex.get(url);
	}

	public static void clear() {
		cursor = primaryIndex.entities();
		try {
		     for (URLVisited entity = cursor.first();
		                   entity != null;
		                   entity = cursor.next()) {
		         cursor.delete();
		     }
		 } finally {
		     cursor.close();
		 }
	}
	
	public static void close() {
		if (URLCrawleredDA.store != null) {
			URLCrawleredDA.store.close();
		}
	}
}
