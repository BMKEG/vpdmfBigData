package edu.isi.bmkeg.vpdmf.solr.specs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="cores")
public class CoreListSpec {
	
	private String adminPath = "/admin/cores";

	private List<CoreSpec> coreList = new ArrayList<CoreSpec>();
	
	@XmlAttribute
	public String getAdminPath() {
		return adminPath;
	}

	public void setAdminPath(String adminPath) {
		this.adminPath = adminPath;
	}

    @XmlElement( name="core" )
	public List<CoreSpec> getCoreList() {
		return coreList;
	}

	public void setCoreList(List<CoreSpec> coreList) {
		this.coreList = coreList;
	}
	
}
