package edu.isi.bmkeg.vpdmf.solr.specs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.isi.bmkeg.vpdmf.model.definitions.specs.FormControlSpec;

@XmlRootElement(name="fieldType")
public class TypeSpec {
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// general characteristics 
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private String name;
	
	private String className;

	private Boolean sortMissingLast;
	
	private Boolean indexed;

	private Boolean stored;

	private Boolean multiValued;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// characteristics of numeric fields 
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	private Integer precisionStep;

	private Integer positionIncrementGap;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// characteristics of text fields 
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private List<AnalyzerSpec> analyzers = new ArrayList<AnalyzerSpec>();

	private Boolean autoGeneratePhraseQueries;

	private Boolean omitNoms;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// characteristics of point fields 
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private Integer dimension;

	private String subFieldSuffix;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// characteristics of currency fields 
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private String defaultCurrency;
	private String currencyConfig;

	
	// _________________________________________________________________________________
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name="class")
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@XmlAttribute
	public Boolean getSortMissingLast() {
		return sortMissingLast;
	}

	public void setSortMissingLast(Boolean sortMissingLast) {
		this.sortMissingLast = sortMissingLast;
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
	public Boolean getMultiValued() {
		return multiValued;
	}

	public void setMultiValued(Boolean multiValued) {
		this.multiValued = multiValued;
	}

	@XmlAttribute
	public Integer getPrecisionStep() {
		return precisionStep;
	}

	public void setPrecisionStep(Integer precisionStep) {
		this.precisionStep = precisionStep;
	}

	@XmlAttribute
	public Integer getPositionIncrementGap() {
		return positionIncrementGap;
	}

	public void setPositionIncrementGap(Integer positionIncrementGap) {
		this.positionIncrementGap = positionIncrementGap;
	}

	@XmlElement( name="analyzer" )
    public List<AnalyzerSpec> getAnalyzers() {
		return analyzers;
	}

	public void setAnalyzers(List<AnalyzerSpec> analyzers) {
		this.analyzers = analyzers;
	}

	@XmlAttribute
	public Boolean getAutoGeneratePhraseQueries() {
		return autoGeneratePhraseQueries;
	}

	public void setAutoGeneratePhraseQueries(Boolean autoGeneratePhraseQueries) {
		this.autoGeneratePhraseQueries = autoGeneratePhraseQueries;
	}

	@XmlAttribute
	public Boolean getOmitNoms() {
		return omitNoms;
	}

	public void setOmitNoms(Boolean omitNoms) {
		this.omitNoms = omitNoms;
	}

	@XmlAttribute
	public Integer getDimension() {
		return dimension;
	}

	public void setDimension(Integer dimension) {
		this.dimension = dimension;
	}

	@XmlAttribute
	public String getSubFieldSuffix() {
		return subFieldSuffix;
	}

	public void setSubFieldSuffix(String subFieldSuffix) {
		this.subFieldSuffix = subFieldSuffix;
	}
	
	@XmlAttribute
	public String getDefaultCurrency() {
		return defaultCurrency;
	}

	public void setDefaultCurrency(String defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}

	@XmlAttribute
	public String getCurrencyConfig() {
		return currencyConfig;
	}

	public void setCurrencyConfig(String currencyConfig) {
		this.currencyConfig = currencyConfig;
	}
	
	
}
