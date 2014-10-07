package edu.isi.bmkeg.vpdmf.solr.specs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="analyzer")
public class AnalyzerSpec {
	
	private String type;
	
	private List<AnalyzerComponentSpec> components = new ArrayList<AnalyzerComponentSpec>();

	@XmlAttribute
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    @XmlElementRefs({
        @XmlElementRef(name = "tokenizer", type = TokenizerSpec.class),
        @XmlElementRef(name = "charFilter", type = CharFilterSpec.class),
        @XmlElementRef(name = "filter", type = FilterSpec.class)
        })
    @XmlMixed
	public List<AnalyzerComponentSpec> getComponents() {
		return components;
	}

	public void setComponents(List<AnalyzerComponentSpec> components) {
		this.components = components;
	}
	
	public void cleanUpElements() {
		
		List<AnalyzerComponentSpec> l = new ArrayList<AnalyzerComponentSpec>();
		
		Iterator it = this.components.iterator();
		while(it.hasNext()) {
			Object o = it.next();
			if( (o instanceof AnalyzerComponentSpec) ) {
				l.add( (AnalyzerComponentSpec) o);
			}
		}	
		
		this.components = l;

	}

}
