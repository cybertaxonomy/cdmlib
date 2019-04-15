/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dto;

import java.util.Set;

import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;

/**
 * @author andreas
 * @since Mar 25, 2015
 *
 */
public interface ITermRepresentation_L10n {

    public abstract String getLabel();

    public abstract String getAbbreviatedLabel();

    public abstract String getText();

    public String getLanguageIso();

    public String getLanguageUuid();

    /**
     * Derives the localized representations from the given term and sets
     * the according fields of the  TermRepresentation_L10n instance
     *
     * @param term
     * @param useInverseRepresentation
     */
    public abstract void localize(TermBase term, boolean useInverseRepresentation);

    void localize(Set<Representation> representations);

}
