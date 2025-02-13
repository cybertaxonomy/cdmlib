/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.FieldBridge;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.hibernate.search.NotNullAwareIdBridge;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.description.SpecimenDescriptionDefaultCacheStrategy;

/**
 * This class represents descriptions for {@link SpecimenOrObservationBase specimens or observations}.
 * <P>
 * This class corresponds to DescriptionsBaseType with an "Object" element
 * according to the SDD schema.
 *
 * @author a.mueller
 * @since 08-Jul-2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecimenDescription", propOrder = {
        "describedSpecimenOrObservation"
})
@XmlRootElement(name = "SpecimenDescription")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionBase")
@Audited
@Configurable
public class SpecimenDescription
        extends DescriptionBase<IIdentifiableEntityCacheStrategy<SpecimenDescription>>{

	private static final long serialVersionUID = -8506790426682192703L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();


    @XmlElement( name = "DescribedSpecimenOrObservation")
    @ManyToOne(fetch = FetchType.LAZY)
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @JoinColumn(name="specimen_id")
    @FieldBridge(impl=NotNullAwareIdBridge.class)
    private SpecimenOrObservationBase<?> describedSpecimenOrObservation;


// ************************ FACTORY *******************************/

    public static SpecimenDescription NewInstance(){
        return new SpecimenDescription();
    }

    public static SpecimenDescription NewInstance(SpecimenOrObservationBase specimen){
        SpecimenDescription description =  new SpecimenDescription();
        description.setDescribedSpecimenOrObservation(specimen);
        return description;
    }

// ***************************** CONSTRUCTOR *********************/
    public SpecimenDescription() {
		super();
	}

    @Override
    protected void initDefaultCacheStrategy() {
        this.cacheStrategy = SpecimenDescriptionDefaultCacheStrategy.NewInstance();
    }

    @Override
    public IDescribable<?> describedEntity(){
        return this.getDescribedSpecimenOrObservation();
    }

//****************************** GETTER / SETTER ***********************************/

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

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> specimen description. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> specimen description by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.description.DescriptionBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SpecimenDescription clone() {
		SpecimenDescription result;
		result = (SpecimenDescription)super.clone();
		//no changes to: taxonName
		return result;
	}
}