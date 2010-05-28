package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Occurrences", propOrder = {
	    "occurrences"
})
@XmlRootElement(name = "Occurrences")
public class Occurrences extends CdmListWrapper<SpecimenOrObservationBase> {
	
	@XmlElements({
    	@XmlElement(name = "DerivedUnit", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DerivedUnit.class),
    	@XmlElement(name = "DnaSample", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DnaSample.class),
    	@XmlElement(name = "FieldObservation", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = FieldObservation.class),
    	@XmlElement(name = "Fossil", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = Fossil.class),
    	@XmlElement(name = "LivingBeing", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = LivingBeing.class),
    	@XmlElement(name = "Observation", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = Observation.class),
    	@XmlElement(name = "Specimen", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = Specimen.class)
    })
	protected List<SpecimenOrObservationBase> occurrences = new ArrayList<SpecimenOrObservationBase>();

	@Override
	public List<SpecimenOrObservationBase> getElements() {
		return occurrences;
	}

	@Override
	public void setElements(List<SpecimenOrObservationBase> elements) {
		this.occurrences = elements;
	}

}
