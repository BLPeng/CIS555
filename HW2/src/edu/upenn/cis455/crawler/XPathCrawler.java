package edu.upenn.cis455.crawler;


public class XPathCrawler {
	
	public static void main(String args[]) {
		int size = args.length;
		String url;
		String dir;
		int maxSize;
		int numOfFiles = 10;			// default 10 pages
		if (size < 3) {
			System.out.println("Usage:");
			System.out.println("1.The URL of the Web page at which to start");
			System.out.println("2.The directory containing the BerkeleyDB");
			System.out.println("3.The maximum size, in megabytes");
			System.out.println("4.[optional]the number of files");
			return;
		}
		// read input
		if (size >= 3) {
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
					System.err.println("Invalid number " + args[2]);
					return;
				}				
			}
			CrawlerWorker crawler = new CrawlerWorker();
			crawler.setUrl(url);
			crawler.setDir(dir);
			crawler.setMaxSize(maxSize);
			crawler.setMaxPage(numOfFiles);
			crawler.start();
		}
	}
}
