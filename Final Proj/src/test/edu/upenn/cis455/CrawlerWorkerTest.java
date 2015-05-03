package test.edu.upenn.cis455;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.upenn.cis455.crawler.CrawlerWorker;
import edu.upenn.cis455.crawler.CrawlerWorkerPool;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.ContentDA;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.RobotInfoDA;

public class CrawlerWorkerTest extends TestCase{
	private CrawlerWorker worker;
	private CrawlerWorkerPool pool;
	private BlockingQueue<String> pendingURLs;
	
	@BeforeClass
	public void setUp() {
		DBWrapper.setupDirectory("testDatabase");
		pool = new CrawlerWorkerPool();
		pendingURLs = new ArrayBlockingQueue<String>(100);
	}
	@Test
	public void testFetchDocumentHTML() {
		String url = "https://dbappserv.cis.upenn.edu/crawltest.html";
		ContentDA.deleteEntry(url);
		worker = new CrawlerWorker(pool, pendingURLs, new HashSet<String>(), 1);
		worker.crawlPage(url);
		assertNotNull(ContentDA.getEntry(url));
	}
	@Test
	public void testFetchDocumentXML() {
		String url = "https://dbappserv.cis.upenn.edu/crawltest/misc/weather.xml";
		ContentDA.deleteEntry(url);
		worker = new CrawlerWorker(pool, pendingURLs, new HashSet<String>(), 1);
		worker.crawlPage(url);
		assertNotNull(ContentDA.getEntry(url));
	}
	@Test
	public void testExtractLinks() {
		String url = "https://dbappserv.cis.upenn.edu/crawltest.html";
		ContentDA.deleteEntry(url);
		worker = new CrawlerWorker(pool, pendingURLs, new HashSet<String>(), 1);
		assertTrue(worker.getPendingURLs().size() == 0);
		worker.crawlPage(url);
		assertTrue(worker.getPendingURLs().size() == 9); // this page contains 9 links if the page is not changed
	}
	@Test
	public void testMatchChannel() {
		String url = "https://dbappserv.cis.upenn.edu/crawltest/misc/weather.xml";
		ContentDA.deleteEntry(url);
		String[] xpaths = {"/dwml/head/product[@concise-name=\"time-series\"]"};
		Channel channel = new Channel("1", "aa", "xsl-url", new Date(), xpaths);
		ChannelDA.putEntry(channel);
		worker = new CrawlerWorker(pool, pendingURLs, new HashSet<String>(), 1);
		assertTrue(channel.getXmlFiles().length == 0);
		worker.crawlPage(url);
		channel = ChannelDA.getEntry("1");
		assertTrue(channel.getXmlFiles().length == 1); // this page contains 9 links if the page is not changed
	}
	@AfterClass
    public void tearDown() {
		DBWrapper.closeDBs();
    }

}
