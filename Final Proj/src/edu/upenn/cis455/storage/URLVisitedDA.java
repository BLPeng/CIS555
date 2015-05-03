package edu.upenn.cis455.storage;


import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

// class to access User database
public class URLVisitedDA {
	public static EntityStore store;
	private static PrimaryIndex<String, URLVisited> primaryIndex;
	public static String envDirectory = "data/channelDB";
    
	public static PrimaryIndex<String, URLVisited> getPrimaryIndex() {
		return primaryIndex;
	}

	public static void setPrimaryIndex(PrimaryIndex<String, URLVisited> primaryIndex) {
		URLVisitedDA.primaryIndex = primaryIndex;
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
		URLVisitedDA.store = new EntityStore(env, "URLVisitedStore", storeConfig);
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
		return primaryIndex.contains(url);
	}
	
	public static URLVisited getEntry(String url) {
		return primaryIndex.get(url);
	}

	
	public static void close() {
		if (URLVisitedDA.store != null) {
				try {
					
					URLVisitedDA.store.close();
				} catch (Exception e) {
					
				}
				
			
		}
	}
}
