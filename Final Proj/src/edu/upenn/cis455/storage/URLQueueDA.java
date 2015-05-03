package edu.upenn.cis455.storage;


import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

// class to access User database
public class URLQueueDA {
	public static EntityStore store;
	private static long urlID;
	private static Object lock;
	private static Object waitObj;
	private static PrimaryIndex<Long, URLQ> primaryIndex;
	private static EntityCursor<URLQ> cursor;
	public static String envDirectory = "data/channelDB";
    
	public static PrimaryIndex<Long, URLQ> getPrimaryIndex() {
		return primaryIndex;
	}

	public static void setPrimaryIndex(PrimaryIndex<Long, URLQ> primaryIndex) {
		URLQueueDA.primaryIndex = primaryIndex;
	}

	public static void init(Environment env)  {

		lock = new Object();
		waitObj = new Object();
		urlID = 0;
	//	EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
	//	envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		storeConfig.setTransactional(false);
		storeConfig.setDeferredWrite(true);
	//	Environment env = new Environment(file, envConfig);
	//	DBWrapper.myEnv = env;
		URLQueueDA.store = new EntityStore(env, "QueueStore", storeConfig);
		primaryIndex = store.getPrimaryIndex(Long.class, URLQ.class);
		DatabaseShutdownHook dbShutdownHook = new DatabaseShutdownHook(env, store);
		Runtime.getRuntime().addShutdownHook(dbShutdownHook);
	}
		
	public static void pushURL(String url) {
		synchronized (lock) {
			urlID++;
			primaryIndex.put(new URLQ(urlID, url));
			lock.notifyAll();
		}	
	}
	
	public static int size() {
		if (primaryIndex == null) {
			return 0;
		}
		return (int) primaryIndex.count();
	}
	
	public static String pollURL() {
		URLQ url = null;
		synchronized (lock) {		
			try {
				while (url == null) {
					cursor = primaryIndex.entities();
					url = cursor.first();
					if (url == null) {
						lock.wait();
					} else {
						cursor.delete();
					}
				}
				
			} catch(Exception e) {
			//	e.printStackTrace();
			} finally {
				try {
					cursor.close();
				} catch (Exception e) {
					
				}
				
			}
			
		}	
		if (url == null) {
			return null;
		}
		return url.getUrl();
	}
	
	public static void close() {
		if (URLQueueDA.store != null) {
			URLQueueDA.store.close();
		}
	}
}
