package edu.upenn.cis455.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class URLRelation {
	@PrimaryKey
	private String Url;
	private String[] urls;

	public URLRelation () {
		
	}
	
	public URLRelation (String url, String[] urls) {
		this.Url = url;
		this.urls = urls;
	}

	public String[] getUrls() {
		return urls;
	}

	public void setUrls(String[] urls) {
		this.urls = urls;
	}

	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}

}
