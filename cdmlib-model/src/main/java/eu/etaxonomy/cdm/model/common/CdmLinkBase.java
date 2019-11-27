/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Base class for subclasses linking to other CDM objects.
 *
 * @author a.mueller
 * @since 09.11.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CdmLink", propOrder = {
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name="CdmLink")
public abstract class CdmLinkBase extends VersionableEntity {

    private static final long serialVersionUID = -1418100748281536524L;

    @XmlElement(name = "Taxon")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    protected TaxonBase<?> taxon;

// ************************* CLONE *******************/

    @Override
    public Object clone() throws CloneNotSupportedException {
        CdmLinkBase result = (CdmLinkBase)super.clone();

        return result;
  }

}
