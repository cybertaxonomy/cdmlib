package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonBases", propOrder = {
	    "taxonBases"
})
@XmlRootElement(name = "TaxonBases")
public class TaxonBases extends CdmListWrapper<TaxonBase> {
	
	@XmlElements({
	  @XmlElement(name = "Taxon", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = Taxon.class),
	  @XmlElement(name = "Synonym", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = Synonym.class)
	})
    protected List<TaxonBase> taxonBases = new ArrayList<TaxonBase>();

	@Override
	public List<TaxonBase> getElements() {
		return taxonBases;
	}

	@Override
	public void setElements(List<TaxonBase> elements) {
		this.taxonBases = elements;
	}

}
