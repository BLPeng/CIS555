package edu.upenn.cis455.storage;


import java.util.Iterator;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.*;

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

	public static void init(Environment env, long id)  {

		lock = new Object();
	//	EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
	//	envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
	//	storeConfig.setTransactional(false);
	//	storeConfig.setDeferredWrite(true);
	//	Environment env = new Environment(file, envConfig);
	//	DBWrapper.myEnv = env;
		URLQueueDA.store = new EntityStore(env, "QueueStore", storeConfig);
		primaryIndex = store.getPrimaryIndex(Long.class, URLQ.class);
		urlID = id;
//		env.removeDatabase(null, "QueueStore");
		DatabaseShutdownHook dbShutdownHook = new DatabaseShutdownHook(env, store);
		Runtime.getRuntime().addShutdownHook(dbShutdownHook);
	}
		
	public static void pushURL(String url) {
		synchronized (lock) {
			urlID++;
//			System.out.println("size" + primaryIndex.count());
			primaryIndex.put(new URLQ(urlID, url));
//			System.out.println("size" + primaryIndex.count());

			lock.notifyAll();
		}	
	}
	
	public static void clear() {
		cursor = primaryIndex.entities();
		try {
		     for (URLQ entity = cursor.first();
		                   entity != null;
		                   entity = cursor.next()) {
		         cursor.delete();
		     }
		 } finally {
		     cursor.close();
		 }
	}
	
	public static int size() {
		if (primaryIndex == null) {
			return 0;
		}
		return (int) primaryIndex.count();
	}
	
	public static URLQ pollURL() {
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
//		System.out.print(url.getId());
		return url;
	}
	
	public static void close() {
		if (URLQueueDA.store != null) {
			try {
				
				URLQueueDA.store.close();
			} catch (Exception e) {
				
			}
			
		}
	}
}
