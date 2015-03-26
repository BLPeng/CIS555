package edu.upenn.cis455.storage;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;

public class DatabaseShutdownHook extends Thread{
	
	private Environment env;
	private EntityStore store;
	
	public DatabaseShutdownHook(Environment env, EntityStore store) {
		this.env = env;
		this.store = store;
	}

	public void run() {
		try {
			if (env != null) {
				store.close();
				env.cleanLog();
				env.close();
			}
		} catch (Exception e) {
			//
		}
	}
}
