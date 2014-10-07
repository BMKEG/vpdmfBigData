package edu.isi.bmkeg.vpdmf.solr.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="uploadProcessTiming")
public class UploadProcessTiming {
	
	private List<TimedCount> submitted = new ArrayList<TimedCount>();

	private List<TimedCount> polled = new ArrayList<TimedCount>();

	private Date startTime = new Date();

    @XmlElementWrapper( name="submitted" )
    @XmlElement( name="timedCount" )
	public List<TimedCount> getSubmitted() {
		return submitted;
	}

	public void setSubmitted(List<TimedCount> submitted) {
		this.submitted = submitted;
	}

    @XmlElementWrapper( name="polled" )
    @XmlElement( name="timedCount" )
	public List<TimedCount> getPolled() {
		return polled;
	}

	public void setPolled(List<TimedCount> polled) {
		this.polled = polled;
	}

	@XmlAttribute
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

}
