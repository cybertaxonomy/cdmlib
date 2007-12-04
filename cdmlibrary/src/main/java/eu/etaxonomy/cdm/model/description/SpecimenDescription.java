package eu.etaxonomy.cdm.model.description;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

@Entity
public class SpecimenDescription extends DescriptionBase {
	static Logger logger = Logger.getLogger(SpecimenDescription.class);
	private Set<SpecimenOrObservationBase> describedSpecimenOrObservations = new HashSet();

	@ManyToMany
	public Set<SpecimenOrObservationBase> getDescribedSpecimenOrObservations() {
		return describedSpecimenOrObservations;
	}
	public void setDescribedSpecimenOrObservations(
			Set<SpecimenOrObservationBase> describedSpecimenOrObservations) {
		this.describedSpecimenOrObservations = describedSpecimenOrObservations;
	}
	public void addDescribedSpecimenOrObservations(SpecimenOrObservationBase describedSpecimenOrObservation) {
		this.describedSpecimenOrObservations.add(describedSpecimenOrObservation);
	}
	public void removeDescribedSpecimenOrObservations(SpecimenOrObservationBase describedSpecimenOrObservation) {
		this.describedSpecimenOrObservations.remove(describedSpecimenOrObservation);
	}



}
