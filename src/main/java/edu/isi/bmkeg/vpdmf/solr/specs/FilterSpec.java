package edu.isi.bmkeg.vpdmf.solr.specs;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="filter")
public class FilterSpec extends AnalyzerComponentSpec {

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Boolean switches
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private Boolean ignoreCase;
	
	private Boolean enablePositionIncrements;

	private Boolean expand;

	private Boolean withOriginal;

	private Boolean inject;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Lookup files 
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private String wordsFile;

	private String synonymsFile;

	private String protectedFile;
	
	private String articlesFile;

	private String dictionaryFile;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// WordDelimiterFilterFactory options 
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private Integer generateWordParts;
	private Integer generateNumberParts;
	private Integer catenateWords;
	private Integer catenateNumbers;
	private Integer catenateAll;
	private Integer splitOnCaseChange;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// other general attributes
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private String encoder;
	
	private String language;

	private String format;

	private Integer minimumLength;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// PatternReplaceFilterFactory options 
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private String pattern;
	private String replacement;
	private String replace;
	
	// _________________________________________________________________________________

	@XmlAttribute
	public Boolean getIgnoreCase() {
		return ignoreCase;
	}
	
	public void setIgnoreCase(Boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	@XmlAttribute
	public Boolean getEnablePositionIncrements() {
		return enablePositionIncrements;
	}

	public void setEnablePositionIncrements(Boolean enablePositionIncrements) {
		this.enablePositionIncrements = enablePositionIncrements;
	}

	@XmlAttribute
	public Boolean getExpand() {
		return expand;
	}

	public void setExpand(Boolean expand) {
		this.expand = expand;
	}

	@XmlAttribute
	public Boolean getWithOriginal() {
		return withOriginal;
	}

	public void setWithOriginal(Boolean withOriginal) {
		this.withOriginal = withOriginal;
	}

	@XmlAttribute
	public Boolean getInject() {
		return inject;
	}

	public void setInject(Boolean inject) {
		this.inject = inject;
	}

	@XmlAttribute(name="words")
	public String getWordsFile() {
		return wordsFile;
	}

	public void setWordsFile(String wordsFile) {
		this.wordsFile = wordsFile;
	}

	@XmlAttribute(name="synonyms")
	public String getSynonymsFile() {
		return synonymsFile;
	}

	public void setSynonymsFile(String synonymsFile) {
		this.synonymsFile = synonymsFile;
	}

	@XmlAttribute(name="protected")
	public String getProtectedFile() {
		return protectedFile;
	}

	public void setProtectedFile(String protectedFile) {
		this.protectedFile = protectedFile;
	}

	@XmlAttribute(name="articles")
	public String getArticlesFile() {
		return articlesFile;
	}

	public void setArticlesFile(String articlesFile) {
		this.articlesFile = articlesFile;
	}

	@XmlAttribute(name="dictionary")
	public String getDictionaryFile() {
		return dictionaryFile;
	}

	public void setDictionaryFile(String dictionaryFile) {
		this.dictionaryFile = dictionaryFile;
	}

	@XmlAttribute
	public Integer getGenerateWordParts() {
		return generateWordParts;
	}

	public void setGenerateWordParts(Integer generateWordParts) {
		this.generateWordParts = generateWordParts;
	}

	@XmlAttribute
	public Integer getGenerateNumberParts() {
		return generateNumberParts;
	}

	public void setGenerateNumberParts(Integer generateNumberParts) {
		this.generateNumberParts = generateNumberParts;
	}

	@XmlAttribute
	public Integer getCatenateWords() {
		return catenateWords;
	}

	public void setCatenateWords(Integer catenateWords) {
		this.catenateWords = catenateWords;
	}

	@XmlAttribute
	public Integer getCatenateNumbers() {
		return catenateNumbers;
	}

	public void setCatenateNumbers(Integer catenateNumbers) {
		this.catenateNumbers = catenateNumbers;
	}

	@XmlAttribute
	public Integer getCatenateAll() {
		return catenateAll;
	}

	public void setCatenateAll(Integer catenateAll) {
		this.catenateAll = catenateAll;
	}

	@XmlAttribute
	public Integer getSplitOnCaseChange() {
		return splitOnCaseChange;
	}

	public void setSplitOnCaseChange(Integer splitOnCaseChange) {
		this.splitOnCaseChange = splitOnCaseChange;
	}

	@XmlAttribute
	public String getEncoder() {
		return encoder;
	}

	public void setEncoder(String encoder) {
		this.encoder = encoder;
	}

	@XmlAttribute
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@XmlAttribute
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@XmlAttribute
	public Integer getMinimumLength() {
		return minimumLength;
	}

	public void setMinimumLength(Integer minimumLength) {
		this.minimumLength = minimumLength;
	}

	@XmlAttribute
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@XmlAttribute
	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	@XmlAttribute
	public String getReplace() {
		return replace;
	}

	public void setReplace(String replace) {
		this.replace = replace;
	}

}
