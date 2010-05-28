package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PolytomousKey;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureTrees", propOrder = {
	    "featureTrees"
})
@XmlRootElement(name = "FeatureTrees")
public class FeatureTrees extends CdmListWrapper<FeatureTree> {

	@XmlElements({
	  @XmlElement(name = "FeatureTree", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = FeatureTree.class),
	  @XmlElement(name = "PolytomousKey", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = PolytomousKey.class)
	})
	protected List<FeatureTree> featureTrees = new ArrayList<FeatureTree>();
	
	@Override
	public List<FeatureTree> getElements() {
		return featureTrees;
	}

	@Override
	public void setElements(List<FeatureTree> elements) {
		this.featureTrees = elements;
	}

}
