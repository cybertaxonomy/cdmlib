package eu.etaxonomy.cdm.remote.dto.tdwg.voc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.Actor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Person", propOrder = {})
@XmlRootElement(name = "Person", namespace = "http://rs.tdwg.org/ontology/voc/Person#")
public class Person extends Actor {

}
