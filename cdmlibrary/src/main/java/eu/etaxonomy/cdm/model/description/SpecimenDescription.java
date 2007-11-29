package eu.etaxonomy.cdm.model.description;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.occurrence.ObservationalUnit;

@Entity
public class SpecimenDescription extends DescriptionBase {
	static Logger logger = Logger.getLogger(SpecimenDescription.class);
	private Set<ObservationalUnit> observationalUnits = new HashSet();

	@ManyToMany
	public Set<ObservationalUnit> getObservationalUnits() {
		return observationalUnits;
	}

	protected void setObservationalUnits(Set<ObservationalUnit> observationalUnits) {
		this.observationalUnits = observationalUnits;
	}

	public void addObservationalUnit(ObservationalUnit observationalUnit) {
		this.observationalUnits.add(observationalUnit);
	}

	public void removeObservationalUnit(ObservationalUnit observationalUnit) {
		this.observationalUnits.remove(observationalUnit);
	}



}
