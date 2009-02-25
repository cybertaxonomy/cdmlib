package eu.etaxonomy.cdm.remote.dto.dc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.voc.LinkType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Relation", propOrder = {})
@XmlRootElement(name = "Relation", namespace = "http://purl.org/dc/elements/1.1/")
public class Relation extends LinkType {

}
