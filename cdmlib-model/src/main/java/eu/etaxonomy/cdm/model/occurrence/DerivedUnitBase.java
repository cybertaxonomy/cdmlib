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
import javax.persistence.FetchType;
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
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.Length;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

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
    "derivationEvent",
    "accessionNumber",
    "collectorsNumber"
})
@XmlRootElement(name = "DerivedUnitBase")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
@Audited
public abstract class DerivedUnitBase<S extends IIdentifiableEntityCacheStrategy> extends SpecimenOrObservationBase<S> implements Cloneable{

	@XmlElement(name = "Collection")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	@IndexedEmbedded
	private Collection collection;

	@XmlElement(name = "CatalogNumber")
	@Field(index=Index.UN_TOKENIZED)
	@NullOrNotEmpty
	@Length(max = 255)
	private String catalogNumber;
	
	@XmlElement(name = "AccessionNumber")
	@Field(index=Index.UN_TOKENIZED)
	@NullOrNotEmpty
	@Length(max = 255)
	private String accessionNumber;
	
	@XmlElement(name = "CollectorsNumber")
	@Field(index=Index.UN_TOKENIZED)
	@NullOrNotEmpty
	@Length(max = 255)
	private String collectorsNumber;
	
	@XmlElement(name = "StoredUnder")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	@IndexedEmbedded
	private TaxonNameBase storedUnder;
	
	@XmlElement(name = "DerivedFrom")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	@IndexedEmbedded(depth = 4)
	private DerivationEvent derivationEvent;

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
		FieldObservation field = (FieldObservation)this.getOriginalUnit();
		field.setGatheringEvent(gatheringEvent);
	}


	public DerivationEvent getDerivedFrom() {
		return derivationEvent;
	}
	
	public void setDerivedFrom(DerivationEvent derivedFrom){
		if (getDerivedFrom() != null){
			getDerivedFrom().getDerivatives().remove(derivedFrom);
		}
		this.derivationEvent = derivedFrom;
		if (derivedFrom != null){
			derivedFrom.addDerivative(this);
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
	
	public void setStoredUnder(TaxonNameBase storedUnder) {
		this.storedUnder = storedUnder;
	}
	
	public String getAccessionNumber() {
		return accessionNumber;
	}
	
	
	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}
	
	public String getCollectorsNumber() {
		return collectorsNumber;
	}
	
	public void setCollectorsNumber(String collectorsNumber) {
		this.collectorsNumber = collectorsNumber;
	}
	
	public TaxonNameBase getStoredUnder() {
		return storedUnder;
	}
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> derivedUnitBase. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> specimen
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link SpecimenOrObservationBase SpecimenOrObservationBase}.
	 * 
	 * @see SpecimenOrObservationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		DerivedUnitBase result = (DerivedUnitBase)super.clone();
		//collection
		result.setCollection(this.collection);
		//derivedFrom
		result.setDerivedFrom(this.derivationEvent);
		//storedUnder
		result.setStoredUnder(this.storedUnder);
		//no changes to: accessionNumber, catalogNumber, collectorsNumber
		return result;
	}
}
