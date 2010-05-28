package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.occurrence.Collection;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Collections", propOrder = {
	    "collections"
})
@XmlRootElement(name = "Collections")
public class Collections extends CdmListWrapper<Collection> {
	
	@XmlElement(name = "Collection", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0")
	protected List<Collection> collections = new ArrayList<Collection>();

	@Override
	public List<Collection> getElements() {
		return collections;
	}

	@Override
	public void setElements(List<Collection> elements) {
		this.collections = elements;
	}

}
