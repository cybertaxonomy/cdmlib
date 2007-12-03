package eu.etaxonomy.cdm.model.description;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservation;

@Entity
public class SpecimenDescription extends DescriptionBase {
	static Logger logger = Logger.getLogger(SpecimenDescription.class);
	private Set<SpecimenOrObservation> describedSpecimenOrObservations = new HashSet();

	@ManyToMany
	public Set<SpecimenOrObservation> getDescribedSpecimenOrObservations() {
		return describedSpecimenOrObservations;
	}
	public void setDescribedSpecimenOrObservations(
			Set<SpecimenOrObservation> describedSpecimenOrObservations) {
		this.describedSpecimenOrObservations = describedSpecimenOrObservations;
	}
	public void addDescribedSpecimenOrObservations(SpecimenOrObservation describedSpecimenOrObservation) {
		this.describedSpecimenOrObservations.add(describedSpecimenOrObservation);
	}
	public void removeDescribedSpecimenOrObservations(SpecimenOrObservation describedSpecimenOrObservation) {
		this.describedSpecimenOrObservations.remove(describedSpecimenOrObservation);
	}



}
