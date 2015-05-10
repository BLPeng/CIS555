package edu.upenn.cis455.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
@Entity
public class Content {
	@PrimaryKey
	private String url;
//	private String content;
	private Date lastAccessedAt;
	private String type;
	
	public Content() {
		
	}
	
	public Content(String url, String content, Date date, String type) {
		this.url = url;
//		this.content = content;
		this.lastAccessedAt = date;
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
/*	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}*/
	public Date getLastAccessedAt() {
		return lastAccessedAt;
	}
	public void setLastAccessedAt(Date lastAccessedAt) {
		this.lastAccessedAt = lastAccessedAt;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return this.url + ", " + this.type + ", " + this.lastAccessedAt.toString();
	}
	
	
	
}
