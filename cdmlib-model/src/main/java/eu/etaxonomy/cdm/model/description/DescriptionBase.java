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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.ClassBridges;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.FieldBridge;

import eu.etaxonomy.cdm.hibernate.search.DescriptionBaseClassBridge;
import eu.etaxonomy.cdm.hibernate.search.GroupByTaxonClassBridge;
import eu.etaxonomy.cdm.hibernate.search.NotNullAwareIdBridge;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * The upmost (abstract) class for a description as a whole (with possibly
 * several {@link DescriptionElementBase elementary information data})
 * for a {@link SpecimenOrObservationBase specimen}, a {@link Taxon taxon}
 * or even a {@link TaxonNameBase taxon name}.
 * <P>
 * This class corresponds to: <ul>
 * <li> DescriptionsSectionType according to the the SDD schema
 * <li> MeasurementOrFact according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptionBase", propOrder = {
    "describedSpecimenOrObservation",
    "descriptionSources",
    "workingSets",
    "descriptionElements",
    "imageGallery"
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@ClassBridges({
    @ClassBridge(impl=DescriptionBaseClassBridge.class),
    @ClassBridge(impl=GroupByTaxonClassBridge.class)
})
public abstract class DescriptionBase<S extends IIdentifiableEntityCacheStrategy> extends IdentifiableEntity<S> {
    private static final long serialVersionUID = 5504218413819040193L;
    private static final Logger logger = Logger.getLogger(DescriptionBase.class);

    @XmlElement( name = "DescribedSpecimenOrObservation")
    @ManyToOne(fetch = FetchType.LAZY)
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @JoinColumn(name="specimen_id")
    @FieldBridge(impl=NotNullAwareIdBridge.class)
    //TODO maybe move down to specific classes SpecimenDescription (with Cascade.Delete) and TaxonDescription (without Cascade)
    private SpecimenOrObservationBase<?> describedSpecimenOrObservation;


    @XmlElementWrapper(name = "DescriptionSources")
    @XmlElement(name = "DescriptionSource")
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @ManyToMany(fetch = FetchType.LAZY)  //FIXME what is the difference between this and IdentifiableEntity.sources
    private Set<Reference> descriptionSources = new HashSet<Reference>();

    @XmlElementWrapper(name = "WorkingSets")
    @XmlElement(name = "WorkingSet")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "descriptions")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set<WorkingSet> workingSets = new HashSet<WorkingSet>();

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
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "inDescription", orphanRemoval=true)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @ContainedIn
    private Set<DescriptionElementBase> descriptionElements = new HashSet<DescriptionElementBase>();

    @XmlElement(name = "ImageGallery")
    private boolean imageGallery;


    /**
     * Returns a {@link SpecimenOrObservationBase specimen or observation} involved in
     * <i>this</i> description as a whole. {@link TaxonDescription Taxon descriptions} are also often based
     * on concrete specimens or observations. For {@link TaxonNameDescription taxon name descriptions}
     * this attribute should be empty.
     * To handle sets of specimen or observations one may first group them by a derivation event of type
     * "Grouping" and then use the grouped unit here.
     * @return
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
			this.describedSpecimenOrObservation.removeDescription(this);
		}else if (! describedSpecimenOrObservation.getDescriptions().contains(this)){
			describedSpecimenOrObservation.addDescription(this);
		}
		this.describedSpecimenOrObservation = describedSpecimenOrObservation;
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
     * @param descriptionSource	the reference source to be added to <i>this</i> description
     * @see    	   				#getDescriptionSources()
     */
    @Deprecated //will probably be removed in future versions due to #2240
    public void addDescriptionSource(Reference descriptionSource) {
        this.descriptionSources.add(descriptionSource);
    }

    /**
     * Removes one element from the set of {@link #getDescriptionSources() references} used as
     * sources for <i>this</i> description.
     *
     * @param  descriptionSource	the reference source which should be deleted
     * @see     		  			#getDescriptionSources()
     * @see     		  			#addDescriptionSource(Reference)
     */
    @Deprecated //will probably be removed in future versions due to #2240
    public void removeDescriptionSource(Reference descriptionSource) {
        this.descriptionSources.remove(descriptionSource);
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
        removeNullValue();
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
        removeNullValue();
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
        removeNullValue();
        this.descriptionElements.remove(element);
        element.setInDescription(null);
    }

    private void removeNullValue(){
        while(this.descriptionElements.contains(null)){
            this.descriptionElements.remove(null);
        }
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

    /**
     * @return the imageGallery
     */
    public boolean isImageGallery() {
        return imageGallery;
    }

    /**
     * @param imageGallery the imageGallery to set
     */
    public void setImageGallery(boolean imageGallery) {
        this.imageGallery = imageGallery;
    }


    public Set<WorkingSet> getWorkingSets() {
        return workingSets;
    }

    public boolean addWorkingSet(WorkingSet workingSet){
        boolean result = this.workingSets.add(workingSet);
        if (! workingSet.getDescriptions().contains(this)){
            workingSet.addDescription(this);
        }
        return result;
    }

    public boolean removeWorkingSet(WorkingSet workingSet){
        boolean result = this.workingSets.remove(workingSet);
        if (workingSet.getDescriptions().contains(this)){
            workingSet.addDescription(this);
        }
        return result;
    }

    protected void setWorkingSets(Set<WorkingSet> workingSets) {
        this.workingSets = workingSets;
    }



    @Transient
    public boolean hasStructuredData(){
        for (DescriptionElementBase element : this.getElements()){
            if (element.isInstanceOf(QuantitativeData.class) ||
                    element.isInstanceOf(CategoricalData.class)){
                return true;
            }
        }
        return false;
    }


//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> descriptioin. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> description by
     * modifying only some of the attributes.<BR>
     *
     * Usages of this name in a taxon concept are NOT cloned.<BR>
     * The name is added to the same homotypical group as the original name
     * (CAUTION: this behaviour needs to be discussed and may change in future).<BR>
     * {@link TaxonNameDescription Name descriptions} are cloned as XXX.<BR>
     * {@link TypeDesignationBase Type designations} are cloned as XXX.<BR>
     * {@link NameRelationship Name relation} are cloned as XXX.<BR>
     *
     * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone()  {
        DescriptionBase<?> result;
        try{
            result = (DescriptionBase<?>)super.clone();

            //working set
            result.workingSets = new HashSet<WorkingSet>();
            for (WorkingSet workingSet : getWorkingSets()){
                workingSet.addDescription(result);
            }

            //descriptions
            result.descriptionSources = new HashSet<Reference>();
            for (Reference reference : getDescriptionSources()){
                result.descriptionSources.add(reference);
            }

            //elements
            result.descriptionElements = new HashSet<DescriptionElementBase>();
            for (DescriptionElementBase element : getElements()){
                DescriptionElementBase newElement = (DescriptionElementBase)element.clone();
                result.addElement(newElement);
            }

            //no changes to: imageGallery
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }

    }
}
