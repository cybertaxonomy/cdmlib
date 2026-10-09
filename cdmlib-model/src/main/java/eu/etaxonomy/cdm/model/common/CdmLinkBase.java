/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

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
        "taxon"
})
@Entity
@Audited
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "CdmLink")
public abstract class CdmLinkBase extends VersionableEntity {

    private static final long serialVersionUID = -1418100748281536524L;

    @XmlElement(name = "Taxon")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    protected TaxonBase taxon;

// ************************* CLONE *******************/

    @Override
    public CdmLinkBase clone() throws CloneNotSupportedException {
        CdmLinkBase result = (CdmLinkBase)super.clone();

        return result;
    }
}
