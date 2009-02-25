package eu.etaxonomy.cdm.remote.dto.tdwg.voc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.DefinedTerm;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonRelationshipTerm", propOrder = {})
@XmlRootElement(name = "TaxonRelationshipTerm", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
public class TaxonRelationshipTerm extends DefinedTerm {

}
