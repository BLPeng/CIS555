package edu.upenn.cis455.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Channel {
	@PrimaryKey
	private String userName;
	private String Url;
	private Date createdAt;
	private String[] xpaths;

	public Channel () {
		
	}
	
	public Channel (String userName, String Url, Date date, String[] xpaths) {
		this.userName = userName;
		this.Url = Url;
		this.createdAt = date;
		this.xpaths = xpaths;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String[] getXpaths() {
		return xpaths;
	}
	public void setXpaths(String[] xpaths) {
		this.xpaths = xpaths;
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	@Override
	public String toString() {
		String xpath = "";
		for (int i = 0; xpaths != null && i < xpaths.length; i++) {
			xpath += xpaths[i] + " ";
		}
		return this.userName + ", " + this.Url + ", " + this.createdAt.toString() + ", " + xpath;
	}
}
