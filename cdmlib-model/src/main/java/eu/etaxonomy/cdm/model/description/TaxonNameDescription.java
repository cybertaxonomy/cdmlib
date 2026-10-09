/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.media.IHasLink;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.description.TaxonNameDescriptionDefaultCacheStrategy;

/**
 * This class represents all piece of information (not ruled by a {@link NomenclaturalCode nomenclatural code})
 * concerning a {@link TaxonName taxon name} like for instance the content of its first
 * publication (protolog) or a picture of this publication.
 *
 * @author a.mueller
 * @since 08-Jul-2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonNameDescription", propOrder = {
        "taxonName"
})
@XmlRootElement(name = "TaxonNameDescription")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed
@Audited
@Configurable
public class TaxonNameDescription
        extends DescriptionBase<IIdentifiableEntityCacheStrategy<TaxonNameDescription>>
        implements IHasLink {

    private static final long serialVersionUID = -7349160369642038687L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @XmlElement(name="TaxonName")
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private TaxonName taxonName;

//******************* FACTORY ********************************************/

    /**
     * Creates a new empty taxon name description instance.
     */
    public static TaxonNameDescription NewInstance(){
        return new TaxonNameDescription();
    }

    /**
     * Creates a new taxon name description instance for the given {@link TaxonName name}.
     * The new taxon name description will be also added to the {@link TaxonName#getDescriptions() set of descriptions}
     * assigned to the given name.
     *
     * @see	#NewInstance()
     */
    public static TaxonNameDescription NewInstance(TaxonName name){
        TaxonNameDescription description = new TaxonNameDescription();
        name.addDescription(description);
        return description;
    }

// ********************** CONSTRUCTOR ***************************************/

    /**
     * Class constructor: creates a new empty taxon name description instance.
     */
    private TaxonNameDescription() {
        super();
    }

    @Override
    protected void initDefaultCacheStrategy() {
        this.cacheStrategy = new TaxonNameDescriptionDefaultCacheStrategy();
    }

//************************* GETTER /SETTER ***************************************/

    /**
     * Returns the {@link TaxonName taxon name} to which <i>this</i> taxon name description
     * provides additional information not ruled by a {@link NomenclaturalCode nomenclatural code}.
     */
    public TaxonName getTaxonName() {
        return taxonName;
    }

//*************** METHODS ***********************************************/

    @Override
    public IDescribable<?> describedEntity(){
        return this.taxonName;
    }

//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> taxon name description. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> taxon name description by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.description.DescriptionBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public TaxonNameDescription clone() {
        TaxonNameDescription result;
        result = (TaxonNameDescription)super.clone();
        //no changes to: taxonName
        return result;
    }
}
