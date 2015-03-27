package edu.upenn.cis455.storage;

import java.util.Date;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


@Entity
public class RobotInfo {
	@PrimaryKey
	private String url;
	private String robotInfo;
	private Date date;
	
	public RobotInfo () {
		
	}
	
	public RobotInfo (String Url, String robotInfo, Date date) {
		this.url = Url;
		this.setRobotInfo(robotInfo);
		this.setDate(date);
	}
	
	@Override
	public String toString() {
		return this.url + ", " + this.robotInfo + ", " + this.date.toString();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getRobotInfo() {
		return robotInfo;
	}

	public void setRobotInfo(String robotInfo) {
		this.robotInfo = robotInfo;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
