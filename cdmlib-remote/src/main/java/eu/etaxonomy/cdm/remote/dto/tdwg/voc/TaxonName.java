package eu.etaxonomy.cdm.remote.dto.tdwg.voc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.Name;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonName", propOrder = {
	    "authorship",
	    "nameComplete"
})
@XmlRootElement(name = "TaxonName", namespace = "http://rs.tdwg.org/ontology/voc/TaxonName#")
public class TaxonName extends Name {
	
	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonName#")
	private String authorship;
	
	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonName#")
	private String nameComplete;

	public String getAuthorship() {
		return authorship;
	}

	public void setAuthorship(String authorship) {
		this.authorship = authorship;
	}

	public String getNameComplete() {
		return nameComplete;
	}

	public void setNameComplete(String nameComplete) {
		this.nameComplete = nameComplete;
	}

}
