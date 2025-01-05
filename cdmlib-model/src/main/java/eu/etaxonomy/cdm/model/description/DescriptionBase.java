/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.ClassBridges;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.BooleanBridge;

import eu.etaxonomy.cdm.hibernate.search.DescriptionBaseClassBridge;
import eu.etaxonomy.cdm.hibernate.search.GroupByTaxonClassBridge;
import eu.etaxonomy.cdm.hibernate.search.NotNullAwareIdBridge;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ICdmTarget;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * The upmost (abstract) class for a description as a whole (with possibly
 * several {@link DescriptionElementBase elementary information data})
 * for a {@link SpecimenOrObservationBase specimen}, a {@link Taxon taxon}
 * or even a {@link TaxonName taxon name}.
 * <P>
 * This class corresponds to: <ul>
 * <li> DescriptionsSectionType according to the the SDD schema
 * <li> MeasurementOrFact according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:24
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptionBase", propOrder = {
    "describedSpecimenOrObservation",
    "descriptionSources",
    "descriptiveDataSets",
    "descriptionElements",
    "imageGallery",
    "isDefault",
    "publish",
    "types"
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@ClassBridges({
    @ClassBridge(impl=DescriptionBaseClassBridge.class),
    @ClassBridge(impl=GroupByTaxonClassBridge.class)
})
public abstract class DescriptionBase<S extends IIdentifiableEntityCacheStrategy>
        extends IdentifiableEntity<S>
        implements ICdmTarget,IPublishable {

    private static final long serialVersionUID = 5504218413819040193L;
    private static final Logger logger = LogManager.getLogger();

    @XmlElement( name = "DescribedSpecimenOrObservation")
    @ManyToOne(fetch = FetchType.LAZY)
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @JoinColumn(name="specimen_id")
    @FieldBridge(impl=NotNullAwareIdBridge.class)
    //TODO maybe move down to specific classes SpecimenDescription (with Cascade.Delete) and TaxonDescription (without Cascade)
    private SpecimenOrObservationBase<?> describedSpecimenOrObservation;


    @XmlElementWrapper(name = "DescriptionSources")
    @XmlElement(name = "DescriptionSource")
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @ManyToMany(fetch = FetchType.LAZY)  //FIXME what is the difference between this and IdentifiableEntity.sources
    @Deprecated
    private Set<Reference> descriptionSources = new HashSet<>();

    @XmlElementWrapper(name = "DescriptiveDataSets")
    @XmlElement(name = "DescriptiveDataSet")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "descriptions")
    private Set<DescriptiveDataSet> descriptiveDataSets = new HashSet<>();

    @XmlElementWrapper(name = "DescriptionElements")
    @XmlElements({
        @XmlElement(name = "CategorialData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = CategoricalData.class),
        @XmlElement(name = "CommonTaxonName", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = CommonTaxonName.class),
        @XmlElement(name = "Distribution", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Distribution.class),
        @XmlElement(name = "IndividualsAssociation", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = IndividualsAssociation.class),
        @XmlElement(name = "QuantitativeData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = QuantitativeData.class),
        @XmlElement(name = "TaxonInteraction", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TaxonInteraction.class),
        @XmlElement(name = "TextData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TextData.class),
        @XmlElement(name = "TemporalData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TemporalData.class)
    })
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "inDescription", orphanRemoval=true)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @ContainedIn
    private Set<DescriptionElementBase> descriptionElements = new HashSet<>();

    @XmlElement(name = "ImageGallery")
    private boolean imageGallery;

    //TODO make it a DescriptionState
    @XmlElement(name = "isDefault")
    private boolean isDefault;

    @XmlElement(name = "publish")
    @Field(analyze = Analyze.NO, store = Store.YES, bridge= @FieldBridge(impl=BooleanBridge.class))
    private boolean publish = true;

    @XmlAttribute(name ="types")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumSetUserType",
        parameters = {@Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.description.DescriptionType")}
    )
    private EnumSet<DescriptionType> types = EnumSet.noneOf(DescriptionType.class);

//******************************** GETTER / SETTER ***************************/

    /**
     * Returns a {@link SpecimenOrObservationBase specimen or observation} involved in
     * <i>this</i> description as a whole. {@link TaxonDescription Taxon descriptions} are also often based
     * on concrete specimens or observations. For {@link TaxonNameDescription taxon name descriptions}
     * this attribute should be empty.
     * To handle sets of specimen or observations one may first group them by a derivation event of type
     * "Grouping" and then use the grouped unit here.
     */
    public SpecimenOrObservationBase getDescribedSpecimenOrObservation() {
		return describedSpecimenOrObservation;
	}

	/**
	 * @see #getDescribedSpecimenOrObservation()
	 * @param describedSpecimenOrObservation
	 */
	//TODO bidirectional method should maybe removed as a description should belong to its specimen or taxon
    public void setDescribedSpecimenOrObservation(SpecimenOrObservationBase describedSpecimenOrObservation) {
		if (describedSpecimenOrObservation == null ){
			if (this.describedSpecimenOrObservation != null){
			    this.describedSpecimenOrObservation.removeDescription(this);
			}
		}else if (! describedSpecimenOrObservation.getDescriptions().contains(this)){
			describedSpecimenOrObservation.addDescription(this);
		}
		this.describedSpecimenOrObservation = describedSpecimenOrObservation;
	}




    /**
     * Returns the set of {@link DescriptionElementBase elementary description data} which constitute
     * <i>this</i> description as a whole.
     *
     * @see    #addElement(DescriptionElementBase)
     * @see    #removeElement(DescriptionElementBase)
     */
    public Set<DescriptionElementBase> getElements() {
        return this.descriptionElements;
    }

    /**
     * Adds an existing {@link DescriptionElementBase elementary description} to the set of
     * {@link #getElements() elementary description data} which constitute <i>this</i>
     * description as a whole.
     * If the elementary descriptions already belongs to a description it is first removed from
     * the old description.
     *
     * @param element	the elementary description to be added to <i>this</i> description
     * @see    	   		#getDescriptionSources()
     */
    public void addElement(DescriptionElementBase element) {
        if (element.getInDescription() != null){
            element.getInDescription().removeElement(element);
        }
        element.setInDescription(this);
        this.descriptionElements.add(element);
    }

    /**
     * Convenience method to add multiple elements.
     * @param elements
     */
    public void addElements(DescriptionElementBase ... elements) {
        for (DescriptionElementBase element : elements){
    		addElement(element);
    	}
    }

    /**
     * Removes one element from the set of {@link #getElements() elementary description data} which
     * constitute <i>this</i> description as a whole.
     *
     * @param  element	the reference source which should be deleted
     * @see     		#getElements()
     * @see     		#addElement(DescriptionElementBase)
     */
    public void removeElement(DescriptionElementBase element) {
        this.descriptionElements.remove(element);
        element.setInDescription(null);
    }



    /**
     * Returns the number of {@link DescriptionElementBase elementary description data} which constitute
     * <i>this</i> description as a whole. This is the cardinality of the set of
     * elementary description data.
     *
     * @see		#getElements()
     * @return	the number of elements of the elementary description data set
     */
    public int size(){
        return this.descriptionElements.size();
    }

    public boolean isImageGallery() {
        return imageGallery;
    }
    public void setImageGallery(boolean imageGallery) {
        this.imageGallery = imageGallery;
    }

    @Override
    public boolean isPublish() {
        return publish;
    }
    @Override
    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isDefault() {
        return isDefault;
    }
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public EnumSet<DescriptionType> getTypes() {
        return types;
    }
    public void setTypes(EnumSet<DescriptionType> types) {
        this.types = types;
    }
    public void addType(DescriptionType type) {
        this.types.add(type);
    }
    public void addTypes(Set<DescriptionType> types) {
        this.types.addAll(types);
    }


    public Set<DescriptiveDataSet> getDescriptiveDataSets() {
        return descriptiveDataSets;
    }
    public boolean addDescriptiveDataSet(DescriptiveDataSet descriptiveDataSet){
        boolean result = this.descriptiveDataSets.add(descriptiveDataSet);
        if (! descriptiveDataSet.getDescriptions().contains(this)){
            descriptiveDataSet.addDescription(this);
        }
        return result;
    }
    public boolean removeDescriptiveDataSet(DescriptiveDataSet descriptiveDataSet){
        boolean result = this.descriptiveDataSets.remove(descriptiveDataSet);
        if (descriptiveDataSet.getDescriptions().contains(this)){
            descriptiveDataSet.removeDescription(this);
        }
        return result;
    }
    protected void setDescriptiveDataSet(Set<DescriptiveDataSet> descriptiveDataSets) {
        this.descriptiveDataSets = descriptiveDataSets;
    }

    /**
     * Returns the set of {@link Reference references} used as sources for <i>this</i> description as a
     * whole. More than one source can be used for a general description without
     * assigning for each data element of the description one of those sources.
     *
     * @see    #addDescriptionSource(Reference)
     * @see    #removeDescriptionSource(Reference)
     */
    @Deprecated //will probably be removed in future versions due to #2240
    public Set<Reference> getDescriptionSources() {
        return this.descriptionSources;
    }

    /**
     * Adds an existing {@link Reference reference} to the set of
     * {@link #getDescriptionSources() references} used as sources for <i>this</i>
     * description.
     *
     * @param descriptionSource the reference source to be added to <i>this</i> description
     * @see                     #getDescriptionSources()
     */
    @Deprecated //will probably be removed in future versions due to #2240
    public void addDescriptionSource(Reference descriptionSource) {
        this.descriptionSources.add(descriptionSource);
    }

    /**
     * Removes one element from the set of {@link #getDescriptionSources() references} used as
     * sources for <i>this</i> description.
     *
     * @param  descriptionSource    the reference source which should be deleted
     * @see                         #getDescriptionSources()
     * @see                         #addDescriptionSource(Reference)
     */
    @Deprecated //will probably be removed in future versions due to #2240
    public void removeDescriptionSource(Reference descriptionSource) {
        this.descriptionSources.remove(descriptionSource);
    }

// *********************** METHODS ******************************/

    @Transient
    //TODO this is not correct
    public boolean hasStructuredData(){
        for (DescriptionElementBase element : this.getElements()){
            if (element.isInstanceOf(QuantitativeData.class) ||
                    element.isInstanceOf(CategoricalData.class)){
                return true;
            }
        }
        return false;
    }

    /**
     * if this is of type {@link DescriptionType#COMPUTED} computed.<BR><BR>
     * Note: Computed is a base type. It has children like {@link DescriptionType#AGGREGATED}.
     * Also for them this method returns <code>true</code>.
     */
    public boolean isComputed() {
        return DescriptionType.isComputed(types);
    }
    public boolean isAggregated() {
        return DescriptionType.includesType(types, DescriptionType.AGGREGATED);
    }
    public boolean isAggregatedDistribution() {
        return isAggregatedDistribution(types);
    }
    public boolean isAggregatedStructuredDescription() {
        return DescriptionType.includesType(types, DescriptionType.AGGREGATED_STRUC_DESC);
    }
    public boolean isCloneForSource() {
        return DescriptionType.includesType(types, DescriptionType.CLONE_FOR_SOURCE);
    }
    public static boolean isDefaultForAggregation(EnumSet<DescriptionType> set) {
        return DescriptionType.includesType(set, DescriptionType.DEFAULT_VALUES_FOR_AGGREGATION);
    }
    public static boolean isSecondaryData(EnumSet<DescriptionType> set) {
        return DescriptionType.includesType(set, DescriptionType.SECONDARY_DATA);
    }
    public static boolean isAggregatedDistribution(EnumSet<DescriptionType> set) {
        return DescriptionType.includesType(set, DescriptionType.AGGREGATED_DISTRIBUTION);
    }

    public abstract IDescribable<?> describedEntity();


//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> description. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> description by
     * modifying only some of the attributes.<BR>
     *
     * Usages of this name in a taxon concept are NOT cloned.<BR>
     * The name is added to the same homotypical group as the original name
     * (CAUTION: this behavior needs to be discussed and may change in future).<BR>
     * {@link TaxonNameDescription Name descriptions} are cloned as XXX.<BR>
     * {@link TypeDesignationBase Type designations} are cloned as XXX.<BR>
     * {@link NameRelationship Name relation} are cloned as XXX.<BR>
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public DescriptionBase<S> clone()  {
        DescriptionBase<S> result;
        try{
            result = (DescriptionBase<S>)super.clone();

            //descriptive dataset
            //TODO do we really want to add the cloned description automatically to the dataset?
            result.descriptiveDataSets = new HashSet<>();
            for (DescriptiveDataSet descriptiveDataSet : getDescriptiveDataSets()){
                descriptiveDataSet.addDescription(result);
            }

            //reference based descriptions
            //TODO remove
            result.descriptionSources = new HashSet<>();
            for (Reference reference : getDescriptionSources()){
                result.descriptionSources.add(reference);
            }

            //elements
            result.descriptionElements = new HashSet<>();
            for (DescriptionElementBase element : getElements()){
                DescriptionElementBase newElement = element.clone();
                result.addElement(newElement);
            }

            result.types = this.types.clone();

            //no changes to: imageGallery
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }

    }
}
