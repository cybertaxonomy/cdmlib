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
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.FieldBridge;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.hibernate.search.NotNullAwareIdBridge;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 * This class represents all piece of information (not ruled by a {@link NomenclaturalCode nomenclatural code})
 * concerning a {@link TaxonName taxon name} like for instance the content of its first
 * publication (protolog) or a picture of this publication.
 *
 * @author a.mueller
 * @created 08-Jul-2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonNameDescription", propOrder = {
        "taxonName"
})
@XmlRootElement(name = "TaxonNameDescription")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionBase")
@Audited
@Configurable
public class TaxonNameDescription extends DescriptionBase<IIdentifiableEntityCacheStrategy<TaxonNameDescription>> implements Cloneable{
    private static final long serialVersionUID = -7349160369642038687L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonNameDescription.class);

    @XmlElement(name="TaxonName")
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @FieldBridge(impl=NotNullAwareIdBridge.class)
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
        this.cacheStrategy = new IdentifiableEntityDefaultCacheStrategy();
    }

//************************* GETTER /SETTER ***************************************/

    /**
     * Returns the {@link TaxonName taxon name} to which <i>this</i> taxon name description
     * provides additional information not ruled by a {@link NomenclaturalCode nomenclatural code}.
     */
    public TaxonName getTaxonName() {
        return taxonName;
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
    public Object clone() {
        TaxonNameDescription result;
        result = (TaxonNameDescription)super.clone();
        //no changes to: taxonName
        return result;
    }
}
