package edu.isi.bmkeg.vpdmf.solr.data;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="timedCount")
public class TimedCount {
	
	private Date time;

	private long count;

	// _________________________________________________________________________________

	@XmlAttribute
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@XmlAttribute
	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

}
