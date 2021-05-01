/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * This
 *
 * @author a.mueller
 * @since 22.04.2021
 */
@Entity
@Audited
public abstract class AvailableForIdentifiableBase<T extends DefinedTermBase>
        extends AvailableForTermBase<T>{

    private static final long serialVersionUID = -8671887501681406910L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AvailableForIdentifiableBase.class);

    //for hibernate use only
    @Deprecated
    protected AvailableForIdentifiableBase() {
        super();
    }
    @Deprecated
    protected AvailableForIdentifiableBase(TermType type) {
        super(type);
    }

    protected AvailableForIdentifiableBase(TermType type, String term, String label, String labelAbbrev) {
        super(type, term, label, labelAbbrev);
    }

// ****************************** GETTER_SETTER *******************************/

    /**
     * Whether this supplement is available for {@link TaxonName taxon names}.
     */
    @XmlElement(name = "AvailableForTaxonName")
    public boolean isAvailableForTaxonName() {
        return getAvailableFor().contains(CdmClass.TAXON_NAME);
    }
    /**
     * @see #isAvailableForTaxon()
     */
    public void setAvailableForTaxonName(boolean availableForTaxonName) {
        setAvailableFor(CdmClass.TAXON_NAME, availableForTaxonName);
    }

    /**
     * Whether this supplement is available for {@link Taxon taxa}.
     */
    @XmlElement(name = "AvailableForTaxon")
    public boolean isAvailableForTaxon() {
        return getAvailableFor().contains(CdmClass.TAXON);
    }
    /**
     * @see #isAvailableForTaxon()
     */
    public void setAvailableForTaxon(boolean availableForTaxon) {
        setAvailableFor(CdmClass.TAXON, availableForTaxon);
    }

    /**
     * Whether this supplement is available for {@link Reference references}.
     */
    @XmlElement(name = "AvailableForReference")
    public boolean isAvailableForReference() {
        return getAvailableFor().contains(CdmClass.REFERENCE);
    }
    /**
     * @see #isAvailableForReference()
     */
    public void setAvailableForReference(boolean availableForReference) {
        setAvailableFor(CdmClass.REFERENCE, availableForReference);
    }


}
