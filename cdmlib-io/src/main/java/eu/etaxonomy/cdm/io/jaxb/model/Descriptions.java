package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Descriptions", propOrder = {
	    "descriptions"
})
@XmlRootElement(name = "Descriptions")
public class Descriptions extends CdmListWrapper<DescriptionBase> {

	@XmlElements({
    	@XmlElement(name = "TaxonDescription", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TaxonDescription.class),
    	@XmlElement(name = "TaxonNameDescription", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TaxonNameDescription.class),
    	@XmlElement(name = "SpecimenDescription", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = SpecimenDescription.class)
    })
	protected List<DescriptionBase> descriptions = new ArrayList<DescriptionBase>();
	
	@Override
	public List<DescriptionBase> getElements() {
		return descriptions;
	}

	@Override
	public void setElements(List<DescriptionBase> elements) {
		this.descriptions = elements;
	}

}
