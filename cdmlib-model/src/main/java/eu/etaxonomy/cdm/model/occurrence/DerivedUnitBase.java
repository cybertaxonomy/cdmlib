/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.Length;

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
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
    "derivedFrom",
    "accessionNumber",
    "collectorsNumber",
    "barcode",
    "specimenTypeDesignations"
})
@XmlRootElement(name = "DerivedUnitBase")
@Entity
@Audited
// even if hibernate complains "Abstract classes can never insert index documents. Remove @Indexed."
// this is needed, otherwise the fields of the also abstract super class are missed during indexing
@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
public abstract class DerivedUnitBase<S extends IIdentifiableEntityCacheStrategy> extends SpecimenOrObservationBase<S> implements Cloneable{

	@XmlElement(name = "Collection")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	@IndexedEmbedded
	private Collection collection;

	@XmlElement(name = "CatalogNumber")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Length(max = 255)
	private String catalogNumber;

	@XmlElement(name = "AccessionNumber")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Length(max = 255)
	private String accessionNumber;

	@XmlElement(name = "CollectorsNumber")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Length(max = 255)
	private String collectorsNumber;

	@XmlElement(name = "Barcode")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Length(max = 255)
	private String barcode;

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
	private DerivationEvent derivedFrom;

	@XmlElementWrapper(name = "SpecimenTypeDesignations")
	@XmlElement(name = "SpecimenTypeDesignation")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "typeSpecimen")
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE })
	private final Set<SpecimenTypeDesignation> specimenTypeDesignations = new HashSet<SpecimenTypeDesignation>();

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
		return derivedFrom;
	}

	public void setDerivedFrom(DerivationEvent derivedFrom){
		if (getDerivedFrom() != null){
			getDerivedFrom().getDerivatives().remove(derivedFrom);
		}
		this.derivedFrom = derivedFrom;
		if (derivedFrom != null){
			derivedFrom.addDerivative(this);
		}
	}

	@Transient
	public Set<SpecimenOrObservationBase> getOriginals(){
		if(getDerivedFrom() != null){
			return getDerivedFrom().getOriginals();
		}
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

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getBarcode() {
		return barcode;
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

	/**
	 * Will be removed in future versions as semantics is not clear.
	 * For accessing the collecting number use
	 * {@link FieldObservation#getFieldNumber()} instead.
	 * @return
	 */
	@Deprecated
	public String getCollectorsNumber() {
		return collectorsNumber;
	}

	/**
	 * Will be removed in future versions as semantics is not clear.
	 * For editing the collecting number use
	 * {@link FieldObservation#getFieldNumber()} instead.
	 * @return
	 */
	@Deprecated
	public void setCollectorsNumber(String collectorsNumber) {
		this.collectorsNumber = collectorsNumber;
	}

	public TaxonNameBase getStoredUnder() {
		return storedUnder;
	}

	public void addSpecimenTypeDesignation(SpecimenTypeDesignation specimenTypeDesignation){
		if (specimenTypeDesignation.getTypeSpecimen() == this){
			return ;
		}else if (specimenTypeDesignation.getTypeSpecimen() != null){
			specimenTypeDesignation.getTypeSpecimen().removeSpecimenTypeDesignation(specimenTypeDesignation);

		}
		specimenTypeDesignations.add(specimenTypeDesignation);
		specimenTypeDesignation.setTypeSpecimen(this);
	}

	public void removeSpecimenTypeDesignation(SpecimenTypeDesignation specimenTypeDesignation){
		if (specimenTypeDesignation == null){
			return;
		}
		if (specimenTypeDesignations.contains(specimenTypeDesignation)){
			specimenTypeDesignations.remove(specimenTypeDesignation);
			specimenTypeDesignation.setTypeSpecimen(null);
		}
	}

	public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations(){
		return specimenTypeDesignations;
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
		result.setDerivedFrom(this.derivedFrom);
		//storedUnder
		result.setStoredUnder(this.storedUnder);
		//no changes to: accessionNumber, catalogNumber, collectorsNumber
		return result;
	}

}
