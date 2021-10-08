/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

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

import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.CdmLinkBase;
import eu.etaxonomy.cdm.model.common.IntextReference;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Class to link to other CdmBase objects within the context of
 * {@link OriginalSourceBase information sources}.
 *
 * @author a.mueller
 * @since 09.11.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CdmLinkSource", propOrder = {
    "description"
})
@XmlRootElement(name = "CdmLinkSource")
@Entity
@Audited
public class CdmLinkSource extends CdmLinkBase {

    private static final long serialVersionUID = 6600576878001716986L;

// // ************* Source ***********************/
//
//    @XmlElement(name = "Source")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
//    @OneToOne(fetch = FetchType.LAZY, mappedBy="cdmSource")
//    private OriginalSourceBase source;

// **************** Targets ************************/

    @XmlElement(name = "Description")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private DescriptionBase<?> description;

// ************************ FACTORY *********************/

    public static CdmLinkSource NewInstance(ICdmTarget target) {
        return new CdmLinkSource(target);
    }

//******************* CONSTRUCTOR *********************/

    public CdmLinkSource(){}  //maybe protected is enough, needs to be tested for loading

    public CdmLinkSource(ICdmTarget target) {
//        this.source = source;
        setTarget(target);
    }

//***************** GETTER / SETTER *****************/

    /**
     * Returns the target object. Throws an {@link IllegalStateException} if no target
     * is defined.
     *
     * @see IntextReference#getTarget()
     */
    public ICdmTarget getTarget() {
        if (taxon != null){
            return CdmBase.deproxy(taxon, Taxon.class);
        }else if (description != null){
            return description;
        }else{
            throw new IllegalStateException("CdmSource has no target object defined");
        }
    }

    public void setTarget(ICdmTarget target) {
        target = CdmBase.deproxy(target);
        if (target instanceof DescriptionBase<?>){
            this.description = (DescriptionBase<?>)target;
        }else if (target instanceof Taxon){
            this.taxon = (Taxon)target;
        }else{
            throw new IllegalArgumentException("Target class not supported by CdmSource");
        }
    }

// ********************************* CLONE **********************************/

    @Override
    public CdmLinkSource clone() throws CloneNotSupportedException {
        CdmLinkSource result = (CdmLinkSource)super.clone();

        return result;
    }

//    public CdmLinkSource clone(OriginalSourceBase source) {
//        CdmLinkSource result;
//        try {
//            result = (CdmLinkSource)super.clone();
////            result.source = source;
//            return result;
//        } catch (CloneNotSupportedException e) {
//            throw new RuntimeException(e);  //does not happen
//        }
//    }

}
