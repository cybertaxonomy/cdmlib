package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.common.Figure;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Media", propOrder = {
	    "media"
})
@XmlRootElement(name = "Media")
public class Media extends CdmListWrapper<eu.etaxonomy.cdm.model.media.Media> {
	
	@XmlElements({
	      @XmlElement(name = "Media", namespace = "http://etaxonomy.eu/cdm/model/media/1.0", type = eu.etaxonomy.cdm.model.media.Media.class),
	      @XmlElement(name = "MediaKey", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = MediaKey.class),
	      @XmlElement(name = "Figure", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = Figure.class),
	      @XmlElement(name = "PhylogeneticTree", namespace = "http://etaxonomy.eu/cdm/model/molecular/1.0", type = PhylogeneticTree.class)
	    })
	    protected List<eu.etaxonomy.cdm.model.media.Media> media = new ArrayList<eu.etaxonomy.cdm.model.media.Media>();

	@Override
	public List<eu.etaxonomy.cdm.model.media.Media> getElements() {
		return media;
	}

	@Override
	public void setElements(List<eu.etaxonomy.cdm.model.media.Media> elements) {
		this.media = elements;
	}

}
