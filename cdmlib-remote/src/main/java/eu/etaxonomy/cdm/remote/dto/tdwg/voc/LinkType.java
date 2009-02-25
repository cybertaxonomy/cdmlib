package eu.etaxonomy.cdm.remote.dto.tdwg.voc;

import java.io.Serializable;
import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
public abstract class LinkType {
	
	@XmlAttribute(namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    protected URI resource;

	public Serializable getResource() {
		return resource;
	}

	public void setResource(URI resource) {
		this.resource = resource;
	}
}
