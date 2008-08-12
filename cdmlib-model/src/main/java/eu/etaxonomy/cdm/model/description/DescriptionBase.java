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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
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

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DescriptionBase extends IdentifiableEntity {
	
	private static final Logger logger = Logger.getLogger(DescriptionBase.class);
	
	@XmlElementWrapper(name = "DescribedSpecimenOrObservations")
	@XmlElement(name = "DescribedSpecimenOrObservation")
	private Set<SpecimenOrObservationBase> describedSpecimenOrObservations = new HashSet<SpecimenOrObservationBase>();
	
	@XmlElementWrapper(name = "DescriptionSources")
	@XmlElement(name = "DescriptionSource")
	private Set<ReferenceBase> descriptionSources = new HashSet<ReferenceBase>();
	
	@XmlElementWrapper(name = "DescriptionElements")
    @XmlElements({
        @XmlElement(name = "CategorialData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = CategoricalData.class),
        @XmlElement(name = "CommonTaxonName", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = CommonTaxonName.class),
        @XmlElement(name = "Distribution", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Distribution.class),
        @XmlElement(name = "IndividualsAssociation", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = IndividualsAssociation.class),
        @XmlElement(name = "QuantitativeData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = QuantitativeData.class),
        @XmlElement(name = "TaxonInteraction", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TaxonInteraction.class),
        @XmlElement(name = "TextData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TextData.class)
    })
	private Set<DescriptionElementBase> descriptionElements = new HashSet<DescriptionElementBase>();

	/**
	 * Returns the set of specimens or observations involved in this description
	 * as a whole. Also taxon descriptions are often based on concrete specimens
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


	@OneToMany(fetch=FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE })
	public Set<DescriptionElementBase> getElements() {
		return this.descriptionElements;
	}

	protected void setElements(Set<DescriptionElementBase> element) {
		this.descriptionElements = element;
	}

	public void addElement(DescriptionElementBase element) {
		this.descriptionElements.add(element);
	}

	public void removeElement(DescriptionElementBase element) {
		this.descriptionElements.remove(element);
	}
	
	@Override
	public String generateTitle() {
		//TODO generate title "generate Title not yet implemented"
		return this.toString();
	}
}
