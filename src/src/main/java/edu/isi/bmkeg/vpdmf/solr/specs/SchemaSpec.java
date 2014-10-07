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

@XmlRootElement(name="schema")
public class SchemaSpec {
	
	private String name;
	
	private String version;

	private List<TypeSpec> types = new ArrayList<TypeSpec>();

	private List<FieldSpec> fields = new ArrayList<FieldSpec>();

	private String uniqueKey;

	private List<CopyFieldSpec> copyFields = new ArrayList<CopyFieldSpec>();

	// _________________________________________________________________________________

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	private String getVersion() {
		return version;
	}

	private void setVersion(String version) {
		this.version = version;
	}

	@XmlElementWrapper( name="types" )
    @XmlElement( name="fieldType" )
	public List<TypeSpec> getTypes() {
		return types;
	}

	public void setTypes(List<TypeSpec> types) {
		this.types = types;
	}

	@XmlElementWrapper( name="fields" )
    @XmlElementRefs( {
        @XmlElementRef(name = "field", type = FieldSpec.class),
        @XmlElementRef(name = "dynamicField", type = DynamicFieldSpec.class)
        })
    @XmlMixed
	public List<FieldSpec> getFields() {
		return fields;
	}

	public void setFields(List<FieldSpec> fields) {
		this.fields = fields;
	}

    @XmlElement
	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	
    @XmlElement( name="copyField" )
	public List<CopyFieldSpec> getCopyFields() {
		return copyFields;
	}

	public void setCopyFields(List<CopyFieldSpec> copyFields) {
		this.copyFields = copyFields;
	}
	
	// _________________________________________________________________________________

	public void cleanUpElements() {
		
		
		Iterator<TypeSpec> tIt = this.types.iterator();
		while(tIt.hasNext()) {
			TypeSpec t = tIt.next();

			Iterator<AnalyzerSpec> aIt = t.getAnalyzers().iterator();
			while(aIt.hasNext()) {
				AnalyzerSpec a = aIt.next();
				a.cleanUpElements();
			
			}
		
		}
		

	}
}
