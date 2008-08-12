/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * http://www.bgbm.org/biodivinf/docs/CollectionModel/ReprintTNR.pdf
 * http://www.bgbm.org/biodivinf/docs/CollectionModel/
 * <BR>
 * Type figures are derived units with at least a figure object in media
 *
 * @author markus
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DerivedUnitBase", propOrder = {
    "collection",
    "catalogNumber",
    "storedUnder",
    "derivedFrom"
})
@XmlRootElement(name = "DerivedUnitBase")
@Entity
public abstract class DerivedUnitBase extends SpecimenOrObservationBase {

	@XmlElement(name = "Collection")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Collection collection;

	@XmlElement(name = "CatalogNumber")
	private String catalogNumber;
	
	@XmlElement(name = "StoredUnder")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private TaxonNameBase storedUnder;
	
	@XmlElement(name = "DerivedFrom")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private DerivationEvent derivedFrom;

	/**
	 * Constructor
	 */
	protected DerivedUnitBase() {
		super();
	}
	/**
	 * create new unit derived from an existing field observation
	 * @param fieldObservation existing field observation from where this unit is derived
	 */
	protected DerivedUnitBase(FieldObservation fieldObservation) {
		super();
		DerivationEvent derivedFrom = new DerivationEvent();
		// TODO: should be done in a more controlled way. Probably by making derivation event implement a general relationship interface (for bidirectional add/remove etc)
		fieldObservation.addDerivationEvent(derivedFrom);
		derivedFrom.getOriginals().add(fieldObservation);
		derivedFrom.getDerivatives().add(this);
		this.setDerivedFrom(derivedFrom);
	}
	/**
	 * create new unit derived from an existing gathering event, 
	 * thereby creating a new empty field observation
	 * @param gatheringEvent the gathering event this unit was collected at 
	 */
	protected DerivedUnitBase(GatheringEvent gatheringEvent) {
		this(new FieldObservation());
		FieldObservation field = (FieldObservation) this.getOriginalUnit();
		field.setGatheringEvent(gatheringEvent);
	}
	
	
	
	@ManyToOne
	@Deprecated //only for bidirectional and persistence use
	private DerivationEvent getDerivationEvent() {
		return getDerivedFrom();
	}
	@Deprecated //only for bidirectional and persistence use
	private void setDerivationEvent(DerivationEvent derivationEvent) {
		this.derivedFrom = derivationEvent;
	}
	@Transient
	public DerivationEvent getDerivedFrom() {
		return derivedFrom;
	}
	public void setDerivedFrom(DerivationEvent derivedFrom){
		if (getDerivedFrom() != null){
			getDerivedFrom().getDerivatives().remove(derivedFrom);
		}
		this.derivedFrom = derivedFrom;
		if (derivedFrom != null){
			derivedFrom.getDerivatives().add(this);
		}
	}
	
	@Transient
	public Set<SpecimenOrObservationBase> getOriginals(){
		return this.getDerivedFrom().getOriginals();
	}


	@Override
	@Transient
	public GatheringEvent getGatheringEvent() {
		// FIXME: implement efficient way of getting original gathering event
		// keep link to original gathering event for performance mainly.
		return null;
	}

	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Collection getCollection(){
		return this.collection;
	}
	public void setCollection(Collection collection){
		this.collection = collection;
	}
	

	public String getCatalogNumber() {
		return catalogNumber;
	}

	public void setCatalogNumber(String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getStoredUnder() {
		return storedUnder;
	}
	public void setStoredUnder(TaxonNameBase storedUnder) {
		this.storedUnder = storedUnder;
	}

}
