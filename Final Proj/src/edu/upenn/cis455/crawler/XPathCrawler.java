package edu.upenn.cis455.crawler;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.URLCrawleredDA;
import edu.upenn.cis455.storage.URLQueueDA;



public class XPathCrawler {
	
	public static void main(String args[]) {
		int size = args.length;
		String url;
		String dir;
		int maxSize;
		int numOfFiles = -1;			// default unlimited pages
		if (size < 1) {
			System.out.println("Usage:");
			System.out.println("1.The URL of the Web page at which to start");
			System.out.println("2.The directory containing the BerkeleyDB");
			System.out.println("3.The maximum size, in megabytes");
			System.out.println("4.[optional]the number of files");
			return;
		} else if (size < 3) {
			url = args[0];
			dir = System.getProperty("user.dir") + "/database";
			maxSize = 1;
			numOfFiles = -1;
		}
		// read input
		else {
			url = args[0];
			dir = args[1];
			try {
				maxSize = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				System.err.println("Invalid number " + args[2]);
				return;
			}
			if (size >= 4) {
				try {
					numOfFiles = Integer.parseInt(args[3]);
				} catch (NumberFormatException e) {
					System.err.println("Invalid number " + args[3]);
					return;
				}				
			}
		}
		DBWrapper.setupDirectory(dir);
		CrawlerWorkerPool crawlerPool = new CrawlerWorkerPool(null);
		URLQueueDA.clear();
		URLCrawleredDA.clear();
		crawlerPool.setUrl(url);
		crawlerPool.setDir(dir);
		crawlerPool.setMaxSize(maxSize);
		crawlerPool.setMaxPage(numOfFiles);
		crawlerPool.start();
	}
}
