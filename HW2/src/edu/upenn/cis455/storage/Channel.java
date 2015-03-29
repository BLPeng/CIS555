package edu.upenn.cis455.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Channel {
	@PrimaryKey
	private String name;
	private String userName;
	private String Url;
	private Date createdAt;
	private String[] xpaths;
	private String[] xmlFiles;

	public Channel () {
		
	}
	
	public Channel (String name, String userName, String Url, Date date, String[] xpaths) {
		this.name = name;
		this.userName = userName;
		this.Url = Url;
		this.createdAt = date;
		this.xpaths = xpaths;
		this.xmlFiles = new String[0];
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
		return this.name + this.userName + ", " + this.Url + ", " + this.createdAt.toString() + ", " + xpath;
	}

	public String[] getXmlFiles() {
		return xmlFiles;
	}

	public void setXmlFiles(String[] xmlFiles) {
		this.xmlFiles = xmlFiles;
	}
}
