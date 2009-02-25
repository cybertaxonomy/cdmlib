package eu.etaxonomy.cdm.remote.dto.tdwg.voc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.Actor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Team", propOrder = {
})
@XmlRootElement(name = "Team", namespace = "http://rs.tdwg.org/ontology/voc/Team#")
public class Team extends Actor {

}
