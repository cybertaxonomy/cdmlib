/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author k.luther
 * @since Jun 11, 2020
 */
public class FeatureDto extends TermDto {

    private static final long serialVersionUID = 5123011385890020838L;

    boolean isAvailableForTaxon = true;
    boolean isAvailableForTaxonName = true;
    boolean isAvailableForOccurrence = true;

    public FeatureDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary, boolean isAvailableForTaxon, boolean isAvailableForTaxonName, boolean isAvailableForOccurrence){
        super(uuid, representations, TermType.Feature, partOfUuid, kindOfUuid,
                vocabularyUuid, orderIndex, idInVocabulary);
        this.isAvailableForOccurrence = isAvailableForOccurrence;
        this.isAvailableForTaxon = isAvailableForTaxon;
        this.isAvailableForTaxonName = isAvailableForTaxonName;

    }

    static public FeatureDto fromTerm(Feature term) {
        FeatureDto result = (FeatureDto) fromTerm(term, null, false);
        result.isAvailableForOccurrence = term.isAvailableForOccurrence();
        result.isAvailableForTaxon = term.isAvailableForTaxon();
        result.isAvailableForTaxonName = term.isAvailableForTaxonName();
        return result;
    }

    /**
     * @return the isAvailableForTaxon
     */
    public boolean isAvailableForTaxon() {
        return isAvailableForTaxon;
    }

    /**
     * @param isAvailableForTaxon the isAvailableForTaxon to set
     */
    public void setAvailableForTaxon(boolean isAvailableForTaxon) {
        this.isAvailableForTaxon = isAvailableForTaxon;
    }

    /**
     * @return the isAvailableForTaxonName
     */
    public boolean isAvailableForTaxonName() {
        return isAvailableForTaxonName;
    }

    /**
     * @param isAvailableForTaxonName the isAvailableForTaxonName to set
     */
    public void setAvailableForTaxonName(boolean isAvailableForTaxonName) {
        this.isAvailableForTaxonName = isAvailableForTaxonName;
    }

    /**
     * @return the isAvailableForOccurrence
     */
    public boolean isAvailableForOccurrence() {
        return isAvailableForOccurrence;
    }

    /**
     * @param isAvailableForOccurrence the isAvailableForOccurrence to set
     */
    public void setAvailableForOccurrence(boolean isAvailableForOccurrence) {
        this.isAvailableForOccurrence = isAvailableForOccurrence;
    }


}
