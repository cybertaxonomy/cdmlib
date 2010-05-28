package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermVocabularies", propOrder = {
	    "termVocabularies"
})
@XmlRootElement(name = "TermVocabularies")
public class TermVocabularies extends CdmListWrapper<TermVocabulary<DefinedTermBase>> {

	@XmlElements({
        @XmlElement(name = "TermVocabulary", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = TermVocabulary.class),
        @XmlElement(name = "OrderedTermVocabulary", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = OrderedTermVocabulary.class)
    })
	List<TermVocabulary<DefinedTermBase>> termVocabularies = new ArrayList<TermVocabulary<DefinedTermBase>>();
	
	@Override
	public List<TermVocabulary<DefinedTermBase>> getElements() {
		return termVocabularies;
	}

	@Override
	public void setElements(List<TermVocabulary<DefinedTermBase>> elements) {
		this.termVocabularies = elements;
	}

}
