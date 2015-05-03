package edu.upenn.cis455.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

// class to access User database
public class ChannelDA {
	public static EntityStore store;
	private static PrimaryIndex<String, Channel> primaryIndex;
	public static String envDirectory = "data/channelDB";
    
	public static PrimaryIndex<String, Channel> getPrimaryIndex() {
		return primaryIndex;
	}

	public static void setPrimaryIndex(PrimaryIndex<String, Channel> primaryIndex) {
		ChannelDA.primaryIndex = primaryIndex;
	}

	public static void init(Environment env)  {
		// absolute path from where the application was initialized.
	/*	String dir = System.getProperty("user.dir");
		File file = new File(dir, DBWrapper.envDirectory);
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
	//	Environment env = new Environment(file, envConfig);
	//	DBWrapper.myEnv = env;
		ChannelDA.store = new EntityStore(env, "ChannelStore", storeConfig);
		primaryIndex = store.getPrimaryIndex(String.class, Channel.class);
		
		DatabaseShutdownHook dbShutdownHook = new DatabaseShutdownHook(env, store);
		Runtime.getRuntime().addShutdownHook(dbShutdownHook);
	}
		
	public static void putEntry(Channel channel) {
		primaryIndex.put(channel);
	}
	
	public static Channel getEntry(String userName) {
		return primaryIndex.get(userName);
	}
	
	public static void deleteEntry(String userName) {
		primaryIndex.delete(userName);
	}

	public static boolean containsEntry(String userName) {
		return primaryIndex.contains(userName);
	}
	
	public static List<Channel> getEntries() {
		EntityCursor<Channel> pi_cursor = primaryIndex.entities();
		List<Channel> ret = new ArrayList<Channel>();
		try {
		    for (Channel seci : pi_cursor) {
		    	ret.add(seci);
		    }
		// Always make sure the cursor is closed when we are done with it.
		} finally {
		pi_cursor.close();
		}
		return ret;
	}

	public static void addXML(String name, String xml) {
		Channel channel = primaryIndex.get(name);
		if (channel == null) {
			return;
		}
		String[] existXMLs = channel.getXmlFiles();
		List<String> newXMLs = new ArrayList<String>();
		newXMLs.add(xml);
		for (String xml1 : existXMLs) {
			newXMLs.add(xml1);
		}
		String[] newXMLs1 = new String[newXMLs.size()];
		for (int i  = 0; i < newXMLs.size(); i++) {
			newXMLs1[i] = newXMLs.get(i);
		}
		channel.setXmlFiles(newXMLs1);
		ChannelDA.putEntry(channel);
	}
	public static void addXMLs(String name, List<String> xmls) {
		Channel channel = primaryIndex.get(name);
		if (channel == null) {
			return;
		}
		String[] existXMLs = channel.getXmlFiles();
		List<String> newXMLs = new ArrayList<String>();
		newXMLs.addAll(xmls);
		for (String xml : existXMLs) {
			newXMLs.add(xml);
		}
		String[] newXMLs1 = new String[newXMLs.size()];
		for (int i  = 0; i < newXMLs.size(); i++) {
			newXMLs1[i] = newXMLs.get(i);
		}
		channel.setXmlFiles(newXMLs1);
		ChannelDA.putEntry(channel);
	}
	
	public static void close() {
		if (ChannelDA.store != null) {
			ChannelDA.store.close();
		}
	}
}
