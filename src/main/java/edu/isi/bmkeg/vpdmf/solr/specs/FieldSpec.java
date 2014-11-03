package edu.isi.bmkeg.vpdmf.solr.specs;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="field")
public class FieldSpec {

	private String name;

	private String type;

	private String defaultString;

	private Boolean indexed;

	private Boolean stored;

	private Boolean omitNorms;

	private Boolean multiValued;

	private Boolean termVectors;
	private Boolean termPositions;
	private Boolean termOffsets;
	
		
	// _________________________________________________________________________________

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlAttribute(name="default")	
	public String getDefaultString() {
		return defaultString;
	}

	public void setDefaultString(String defaultString) {
		this.defaultString = defaultString;
	}

	@XmlAttribute
	public Boolean getIndexed() {
		return indexed;
	}

	public void setIndexed(Boolean indexed) {
		this.indexed = indexed;
	}

	@XmlAttribute
	public Boolean getStored() {
		return stored;
	}

	public void setStored(Boolean stored) {
		this.stored = stored;
	}

	@XmlAttribute
	public Boolean getOmitNorms() {
		return omitNorms;
	}

	public void setOmitNorms(Boolean omitNorms) {
		this.omitNorms = omitNorms;
	}

	@XmlAttribute
	public Boolean getMultiValued() {
		return multiValued;
	}

	public void setMultiValued(Boolean multiValued) {
		this.multiValued = multiValued;
	}

	@XmlAttribute
	public Boolean getTermVectors() {
		return termVectors;
	}

	public void setTermVectors(Boolean termVectors) {
		this.termVectors = termVectors;
	}

	@XmlAttribute
	public Boolean getTermPositions() {
		return termPositions;
	}

	public void setTermPositions(Boolean termPositions) {
		this.termPositions = termPositions;
	}

	@XmlAttribute
	public Boolean getTermOffsets() {
		return termOffsets;
	}

	public void setTermOffsets(Boolean termOffsets) {
		this.termOffsets = termOffsets;
	}

}
