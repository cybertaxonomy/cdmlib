/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * The upmost (abstract) class for the whole description with possibly several
 * feature data of a specimen or of a taxon.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */

@MappedSuperclass
public abstract class DescriptionBase extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(DescriptionBase.class);
	
	private Set<SpecimenOrObservationBase> describedSpecimenOrObservations = new HashSet<SpecimenOrObservationBase>();
	private Set<ReferenceBase> descriptionSources = new HashSet<ReferenceBase>();
	private Set<DescriptionElementBase> features = new HashSet<DescriptionElementBase>();

	/**
	 * Returns the set of specimens or observations involved in this description
	 * as a whole. Also taxa descriptions are often based on concrete specimens
	 * or observations. 
	 * 
	 * @return	the set of of specimens or observations 
	 */
	//@ManyToMany  //FIXME
	@Transient 
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

	/**
	 * Returns the set of references used as sources for this description as a
	 * whole. More than one source can be used for a general description without
	 * assigning for each data element of the description one of those sources. 
	 * 
	 * @return	the set of references 
	 */
//	@ManyToMany  //FIXME
//	@Cascade( { CascadeType.SAVE_UPDATE })
	@Transient
	public Set<ReferenceBase> getDescriptionSources() {
		return this.descriptionSources;
	}
	protected void setDescriptionSources(Set<ReferenceBase> descriptionSources) {
		this.descriptionSources = descriptionSources;
	}
	public void addDescriptionSource(ReferenceBase descriptionSource) {
		this.descriptionSources.add(descriptionSource);
	}
	public void removeDescriptionSource(ReferenceBase descriptionSource) {
		this.descriptionSources.remove(descriptionSource);
	}


	@OneToMany
	@Cascade( { CascadeType.SAVE_UPDATE })
	public Set<DescriptionElementBase> getFeatures() {
		return this.features;
	}

	protected void setFeatures(Set<DescriptionElementBase> features) {
		this.features = features;
	}

	public void addFeature(DescriptionElementBase feature) {
		this.features.add(feature);
	}

	public void removeFeature(DescriptionElementBase feature) {
		this.features.remove(feature);
	}
	
	@Override
	public String generateTitle() {
		logger.warn("generate Title not yet implemented");
		return "generate Title not yet implemented";
	}
}
