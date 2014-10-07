package edu.isi.bmkeg.vpdmf.solr.specs;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="tokenizer")
public class TokenizerSpec extends AnalyzerComponentSpec {

	private String mode;

	@XmlAttribute
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	
	
}
