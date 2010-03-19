package eu.etaxonomy.cdm.remote.dto.oaipmh;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum MetadataPrefix {
	@XmlEnumValue("rdf")
	RDF("rdf"), 
	@XmlEnumValue("oai_dc")
	OAI_DC("oai_dc");
	
	private String value;
	
	private MetadataPrefix(String value) {
		this.value = value;
	}
	
	protected  String value() {
		return value;
	}	

	public static MetadataPrefix value(String string) {
		for(MetadataPrefix m : MetadataPrefix.values()) {
			if(m.value().equals(string)) {
				return m;
			}
		}
		throw new IllegalArgumentException();
	}
}
