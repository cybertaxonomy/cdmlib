/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmClass;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

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
    private static final Logger logger = LogManager.getLogger();

    //for hibernate use only, *packet* private required by bytebuddy
    @Deprecated
    AvailableForIdentifiableBase() {}

    @Deprecated
    protected AvailableForIdentifiableBase(TermType type) {
        super(type);
    }

    protected AvailableForIdentifiableBase(TermType type, String term, String label, String labelAbbrev) {
        super(type, term, label, labelAbbrev);
    }

    protected AvailableForIdentifiableBase(TermType type, String term, String label, String labelAbbrev, Language lang) {
        super(type, term, label, labelAbbrev, lang);
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

    /**
     * Whether this supplement is available for {@link Person persons}.
     */
    @XmlElement(name = "AvailableForPerson")
    public boolean isAvailableForPerson() {
        return getAvailableFor().contains(CdmClass.PERSON);
    }
    /**
     * @see #isAvailableForPerson()
     */
    public void setAvailableForPerson(boolean availableForPerson) {
        setAvailableFor(CdmClass.PERSON, availableForPerson);
    }

    /**
     * Whether this supplement is available for {@link SpecimenOrObservationBase occurrences}.
     */
    @XmlElement(name = "AvailableForOccurrence")
    public boolean isAvailableForOccurrence() {
        return getAvailableFor().contains(CdmClass.OCCURRENCE);
    }
    /**
     * @see #isAvailableForOccurrence()
     */
    public void setAvailableForOccurrence(boolean availableForOccurrence) {
        setAvailableFor(CdmClass.OCCURRENCE, availableForOccurrence);
    }
}