package edu.isi.bmkeg.vpdmf.solr.specs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;

import edu.isi.bmkeg.vpdmf.model.definitions.specs.FormControlSpec;

@XmlRootElement(name="solr")
public class SolrSpec {
	
	private CoreListSpec coreList;
	
	private boolean persistent = true;

	private String sharedLib = "lib";
	
    @XmlElement( name="cores" )
	public CoreListSpec getCoreList() {
		return coreList;
	}

	public void setCoreList(CoreListSpec coreList) {
		this.coreList = coreList;
	}

	@XmlAttribute
	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}
	
	@XmlAttribute
	public String getSharedLib() {
		return sharedLib;
	}

	public void setSharedLib(String sharedLib) {
		this.sharedLib = sharedLib;
	}

}
