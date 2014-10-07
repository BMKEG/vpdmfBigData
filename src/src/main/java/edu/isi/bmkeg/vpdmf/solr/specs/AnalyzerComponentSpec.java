package edu.isi.bmkeg.vpdmf.solr.specs;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AnalyzerComponentSpec {

	private String classAddress;

	@XmlAttribute(name="class")
	public String getClassAddress() {
		return classAddress;
	}

	public void setClassAddress(String classAddress) {
		this.classAddress = classAddress;
	}
	
}
