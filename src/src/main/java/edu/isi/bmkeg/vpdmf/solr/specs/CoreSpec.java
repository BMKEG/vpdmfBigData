package edu.isi.bmkeg.vpdmf.solr.specs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="core")
public class CoreSpec {
	
	private String name;
	
	private String instanceDir;
	
	private List<PropertySpec> property = new ArrayList<PropertySpec>();
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getInstanceDir() {
		return instanceDir;
	}

	public void setInstanceDir(String instanceDir) {
		this.instanceDir = instanceDir;
	}

	@XmlElement(name="property")
	public List<PropertySpec> getProperty() {
		return property;
	}

	public void setProperty(List<PropertySpec> property) {
		this.property = property;
	}
	

}
