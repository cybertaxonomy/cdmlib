package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonomicNames", propOrder = {
	    "taxonomicNames"
})
@XmlRootElement(name = "TaxonomicNames")
public class TaxonomicNames extends CdmListWrapper<TaxonNameBase> {
	
	@XmlElements({
    	@XmlElement(name = "BacterialName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = BacterialName.class),
    	@XmlElement(name = "BotanicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = BotanicalName.class),
    	@XmlElement(name = "CultivarPlantName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = CultivarPlantName.class),
    	@XmlElement(name = "NonViralName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NonViralName.class),
    	@XmlElement(name = "ViralName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = ViralName.class),
    	@XmlElement(name = "ZoologicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = ZoologicalName.class)
    })
    protected List<TaxonNameBase> taxonomicNames = new ArrayList<TaxonNameBase>();

	@Override
	public List<TaxonNameBase> getElements() {
		return taxonomicNames;
	}

	@Override
	public void setElements(List<TaxonNameBase> elements) {
		this.taxonomicNames = elements;
	}

}
