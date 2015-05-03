package edu.upenn.cis455.storage;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.*;

// class to access User database
public class URLRelationDA {
	public static EntityStore store;
	private static PrimaryIndex<String, URLRelation> primaryIndex;
	public static String envDirectory = "data/channelDB";
    
	public static PrimaryIndex<String, URLRelation> getPrimaryIndex() {
		return primaryIndex;
	}

	public static void setPrimaryIndex(PrimaryIndex<String, URLRelation> primaryIndex) {
		URLRelationDA.primaryIndex = primaryIndex;
	}

	public static void init(Environment env)  {

	//	EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
	//	envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		storeConfig.setTransactional(false);
	//	Environment env = new Environment(file, envConfig);
	//	DBWrapper.myEnv = env;
		URLRelationDA.store = new EntityStore(env, "URLRelationStore", storeConfig);
		primaryIndex = store.getPrimaryIndex(String.class, URLRelation.class);
//		env.removeDatabase(null, "QueueStore");
		DatabaseShutdownHook dbShutdownHook = new DatabaseShutdownHook(env, store);
		Runtime.getRuntime().addShutdownHook(dbShutdownHook);
	}
		
	public static void putEntry(URLRelation urlR) {
		primaryIndex.put(urlR);
	}
	
	public static URLRelation getEntry(String url) {
		return primaryIndex.get(url);
	}
	
	public static void deleteEntry(String url) {
		primaryIndex.delete(url);
	}

	public static boolean containsEntry(String url) {
		return primaryIndex.contains(url);
	}
	
	public static List<URLRelation> getEntries() {
		EntityCursor<URLRelation> pi_cursor = primaryIndex.entities();
		List<URLRelation> ret = new ArrayList<URLRelation>();
		try {
		    for (URLRelation seci : pi_cursor) {
		    	ret.add(seci);
		    }
		// Always make sure the cursor is closed when we are done with it.
		} finally {
		pi_cursor.close();
		}
		return ret;
	}
	public static void close() {
		if (URLRelationDA.store != null) {
			try {
				
				URLRelationDA.store.close();
			} catch (Exception e) {
				
			}
			
		}
	}
}
