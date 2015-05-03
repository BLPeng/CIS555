package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class URLVisited {
	@PrimaryKey
	private String url;
	private Long time;
	
	public URLVisited () {
		
	}
	
	public URLVisited (Long time, String url) {
		this.time = time;
		this.url = url;
	}
	
	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
