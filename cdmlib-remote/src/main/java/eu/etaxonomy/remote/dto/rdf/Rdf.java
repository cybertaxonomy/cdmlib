//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.08.01 at 10:51:47 AM BST 
//


package eu.etaxonomy.remote.dto.rdf;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.BaseThing;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "things"
})
@XmlRootElement(name = "RDF", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
public class Rdf {

    @XmlElements({
	  @XmlElement(name = "TaxonConcept", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#", type = TaxonConcept.class)
    })
    protected Set<BaseThing> things = new HashSet<BaseThing>();

	public Set<BaseThing> getThings() {
		return things;
	}

	public void addThing(BaseThing thing) {
		this.things.add(thing);
	}
	
	public void removeThing(BaseThing thing) {
		this.things.remove(thing);
	}
}
