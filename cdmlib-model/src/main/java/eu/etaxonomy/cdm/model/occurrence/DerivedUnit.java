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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 * A derived unit is regarded as derived from a field unit,
 * so locality and gathering related information is captured as a separate FieldUnit object
 * related to a specimen via a derivation event
 *
 * http://www.bgbm.org/biodivinf/docs/CollectionModel/ReprintTNR.pdf
 * http://www.bgbm.org/biodivinf/docs/CollectionModel/
 * <BR>
 * Type figures are derived units with at least a figure object in media
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:52
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DerivedUnit", propOrder = {
    "collection",
    "catalogNumber",
    "storedUnder",
    "derivedFrom",
    "accessionNumber",
    "collectorsNumber",
    "barcode",
	"preservation",
	"exsiccatum",
	"originalLabelInfo",
    "specimenTypeDesignations"
})
@XmlRootElement(name = "DerivedUnit")
@Entity
@Audited
// even if hibernate complains "Abstract classes can never insert index documents. Remove @Indexed."
// this is needed, otherwise the fields of the also abstract super class are missed during indexing
@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
public class DerivedUnit
        extends SpecimenOrObservationBase<IIdentifiableEntityCacheStrategy<? extends DerivedUnit>> {

    private static final long serialVersionUID = -3525746216270843517L;
	private static final Logger logger = Logger.getLogger(DerivedUnit.class);

	@XmlElement(name = "Collection")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@IndexedEmbedded
	private Collection collection;

	@XmlElement(name = "CatalogNumber")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Column(length=255)
	private String catalogNumber;

	@XmlElement(name = "AccessionNumber")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Column(length=255)
	private String accessionNumber;

	@XmlElement(name = "CollectorsNumber")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Column(length=255)
	private String collectorsNumber;

	@XmlElement(name = "Barcode")
	@Field(analyze = Analyze.NO)
    //TODO Val #3379
//	@NullOrNotEmpty
	@Column(length=255)
	private String barcode;

	@XmlElement(name = "StoredUnder")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@IndexedEmbedded(includeEmbeddedObjectId=true)
	private TaxonName storedUnder;

	@XmlElement(name = "DerivedFrom")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@IndexedEmbedded(depth = 4)
	private DerivationEvent derivedFrom;

	@XmlElement(name = "OriginalLabelInfo")
	@Lob
    private String originalLabelInfo;

	@XmlElementWrapper(name = "SpecimenTypeDesignations")
	@XmlElement(name = "SpecimenTypeDesignation")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "typeSpecimen")
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE })
	private final Set<SpecimenTypeDesignation> specimenTypeDesignations = new HashSet<SpecimenTypeDesignation>();


//*** attributes valid only for preserved specimen (PreservedSpecimen, Fossil, DnaSample)

	@XmlElement(name = "Preservation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private PreservationMethod preservation;


	@XmlElement(name = "Exsiccatum")
    //TODO Val #3379
//	@NullOrNotEmpty
	@Field
    @Column(length=255)
    private String exsiccatum;

// ******************** FACTORY METHOD **********************************/

	public static DerivedUnit NewInstance(SpecimenOrObservationType type) {
		if (type.isMedia()){
			return MediaSpecimen.NewInstance(type);
		}else if (type.equals(SpecimenOrObservationType.DnaSample) || type.isKindOf(SpecimenOrObservationType.DnaSample)){
			return DnaSample.NewInstance();
		}else if (type.equals(SpecimenOrObservationType.TissueSample) || type.isKindOf(SpecimenOrObservationType.TissueSample)){
            //for now we store TissueSample as DnaSample to allow adding sequences and other DnaSample data to it directly
		    //this is because sometimes explicit DnaSample data does not exist as it is not preserved
		    //In this case a Sequence or Amplification is directly added to the Tissue Sample.
		    //In theory also TissueSample could be missing so this should be possible also for other
		    //SpecimenOrObservationType units.
		    //This is a reason why DnaSample and DerivedUnit should be unified.
		    return DnaSample.NewInstance();
        }else{
			return new DerivedUnit(type);
		}
	}

	public static DerivedUnit NewPreservedSpecimenInstance(){
		DerivedUnit result = new DerivedUnit(SpecimenOrObservationType.PreservedSpecimen);
		return result;
	}

//************************** CONSTRUCTOR *********************************/

	//Constructor: For hibernate use only
	@SuppressWarnings("deprecation")
    protected DerivedUnit() {
	    super();
        initDefaultCacheStrategy();
	}

    /**
	 * Constructor
	 * @param recordBasis
	 */
	protected DerivedUnit(SpecimenOrObservationType recordBasis) {
		super(recordBasis);
        initDefaultCacheStrategy();
	}


	/**
	 * Create new unit derived from an existing field unit
	 * @param fieldUnit existing field unit from where this unit is derived
	 */
	protected DerivedUnit(SpecimenOrObservationType recordBasis, FieldUnit fieldUnit) {
		this(recordBasis);
		DerivationEvent derivedFrom = new DerivationEvent();
		// TODO: should be done in a more controlled way. Probably by making derivation event implement a general relationship interface (for bidirectional add/remove etc)
		fieldUnit.addDerivationEvent(derivedFrom);
		derivedFrom.getOriginals().add(fieldUnit);
		derivedFrom.getDerivatives().add(this);
		this.setDerivedFrom(derivedFrom);
	}

	/**
	 * create new unit derived from an existing gathering event,
	 * thereby creating a new empty field unit
	 * @param gatheringEvent the gathering event this unit was collected at
	 */
	protected DerivedUnit(SpecimenOrObservationType recordBasis, GatheringEvent gatheringEvent) {
		this(recordBasis, new FieldUnit());
		FieldUnit field = (FieldUnit)this.getOriginalUnit();
		field.setGatheringEvent(gatheringEvent);
	}

    private static final String facadeStrategyClassName = "eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy";
    /**
     * Sets the default cache strategy
     */
	@Override
    protected void initDefaultCacheStrategy() {
        try {
            String facadeClassName = facadeStrategyClassName;
            Class<?> facadeClass = Class.forName(facadeClassName);
            try {
                this.cacheStrategy = (IIdentifiableEntityCacheStrategy)facadeClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Cache strategy for DerivedUnit could not be instantiated", e);
            }
        } catch (ClassNotFoundException e) {
            this.cacheStrategy = new IdentifiableEntityDefaultCacheStrategy<>();
        }
    }

// ******************** GETTER / SETTER *************************************/

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
		this.catalogNumber = isBlank(catalogNumber)?null:catalogNumber;
	}

	public void setBarcode(String barcode) {
		this.barcode = isBlank(barcode)? null : barcode;
	}
	public String getBarcode() {
		return barcode;
	}

	public void setStoredUnder(TaxonName storedUnder) {
		this.storedUnder = storedUnder;
	}

	public String getAccessionNumber() {
		return accessionNumber;
	}
	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = isBlank(accessionNumber)? null : accessionNumber;
	}

	/**
	 * Original label information may present the exact original text
	 * or any other text which fully or partly represents the text available
	 * on the specimens label. This information may differ from the information
	 * available in the derived unit itself.
	 * @return the original label information
	 */
	//#4218
	public String getOriginalLabelInfo() {
		return originalLabelInfo;
	}

	public void setOriginalLabelInfo(String originalLabelInfo) {
		this.originalLabelInfo = originalLabelInfo;
	}

	/**
	 * Will be removed in future versions as semantics is not clear.
	 * For accessing the collecting number use
	 * {@link FieldUnit#getFieldNumber()} instead.
	 * @return
	 */
	@Deprecated
	public String getCollectorsNumber() {
		return collectorsNumber;
	}

	/**
	 * Will be removed in future versions as semantics is not clear.
	 * For editing the collecting number use
	 * {@link FieldUnit#getFieldNumber()} instead.
	 * @return
	 */
	@Deprecated
	public void setCollectorsNumber(String collectorsNumber) {
		this.collectorsNumber = isBlank(collectorsNumber)? null : collectorsNumber;
	}

	public TaxonName getStoredUnder() {
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

    public String getMostSignificantIdentifier() {
        if (isNotBlank(getAccessionNumber())) {
            return getAccessionNumber();
        }
        else if(isNotBlank(getBarcode())){
            return getBarcode();
        }
        else if(isNotBlank(getCatalogNumber())){
            return getCatalogNumber();
        }
        return null;
    }

    /**
     * Collects all top most units (FieldUnits, DerivedUnits) in the parent branches of the derivation graph.
     * <p>
     * <b>NOTE:</b> As this method walks the derivation graph it should only be used in a transactional context or
     * it must be assured that the whole graph is initialized.
     *
     * @param typeRestriction
     *  Restricts the returned entities to a specific type of unit.
     */
    public <T extends SpecimenOrObservationBase> java.util.Collection<T> collectRootUnits(Class<T> typeRestriction) {
        return collectRootUnits(typeRestriction, new HashSet<>());
    }


    private <T extends SpecimenOrObservationBase> java.util.Collection<T> collectRootUnits(Class<T> typeRestriction, Set<SpecimenOrObservationBase<?>> cycleDetection) {

        if(typeRestriction == null) {
            typeRestriction = (Class<T>) SpecimenOrObservationBase.class;
        }

        cycleDetection.add(this);

        java.util.Collection<T> rootUnits = new HashSet<>();
        Set<SpecimenOrObservationBase> originals = getOriginals();

        if (originals != null && !originals.isEmpty()) {
            for (SpecimenOrObservationBase<?> original : originals) {
                if (original.isInstanceOf(FieldUnit.class)) {
                    if(typeRestriction.isAssignableFrom(FieldUnit.class)) {
                        if(logger.isTraceEnabled()) {logger.trace(" [" + original + "] <-- " + this );}
                        rootUnits.add(HibernateProxyHelper.deproxy(original, typeRestriction));
                    }
                    // otherwise this entity can be ignored
                } else if(original.isInstanceOf(DerivedUnit.class)){
                    DerivedUnit originalDerivedUnit = HibernateProxyHelper.deproxy(original, DerivedUnit.class);
                    if(!cycleDetection.contains(originalDerivedUnit)) {
                        rootUnits.addAll(originalDerivedUnit.collectRootUnits(typeRestriction, cycleDetection));
                    } else {
                        // circular graph path found, this should not exist but is not prevented by the cdmlib
                        // using this as rootNode
                        if(logger.isTraceEnabled()) {logger.trace("CYCLE! "+ originalDerivedUnit + " <-- [" + this + "] <-- ...");}
                        rootUnits.add((T)this);
                    }
                }
            }
        } else {
            // no more originals for this DerivedUnit.
            // Potential FieldUnits have been filtered out one call before in the recursion
            if(isInstanceOf(typeRestriction)) {
                rootUnits.add(HibernateProxyHelper.deproxy(this, typeRestriction));
            }
        }
        return rootUnits;
    }

// ******* GETTER / SETTER for preserved specimen only ******************/

	public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations(){
		return specimenTypeDesignations;
	}

	public PreservationMethod getPreservation(){
		return this.preservation;
	}

	public void setPreservation(PreservationMethod preservation){
		this.preservation = preservation;
	}


	public void setExsiccatum(String exsiccatum) {
		this.exsiccatum = isBlank(exsiccatum)? null : exsiccatum;
	}

	public String getExsiccatum() {
		return exsiccatum;
	}

//*********** CLONE **********************************/

	/**
	 * Clones <i>this</i> derivedUnit. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> specimen
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link SpecimenOrObservationBase SpecimenOrObservationBase}.
	 *
	 * @see SpecimenOrObservationBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DerivedUnit clone() {
		try{
			DerivedUnit result = (DerivedUnit)super.clone();
			//collection
			result.setCollection(this.collection);
			//derivedFrom
			result.setDerivedFrom(this.derivedFrom);
			//storedUnder
			result.setStoredUnder(this.storedUnder);
			//preservation
			result.setPreservation(this.preservation);
			//no changes to: accessionNumber, catalogNumber, collectorsNumber
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}