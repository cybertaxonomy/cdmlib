/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author andreas
 * @since Mar 25, 2015
 *
 */
public class TermDto implements Serializable{

    private static final long serialVersionUID = 5627308906985438034L;
    private final UUID uuid;
    private UUID partOfUuid = null;
    private UUID vocabularyUuid = null;
    private Integer orderIndex = null;
    private final Set<Representation> representations;
    private String representation_L10n = null;
    private String representation_L10n_abbreviatedLabel = null;
    private Collection<TermDto> includes;

    public TermDto(UUID uuid, Set<Representation> representations, Integer orderIndex) {
        this.representations = representations;
        this.uuid = uuid;
        this.setOrderIndex(orderIndex);
    }

    public TermDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID vocabularyUuid, Integer orderIndex) {
        this.representations = representations;
        this.uuid = uuid;
        this.partOfUuid = partOfUuid;
        this.vocabularyUuid = vocabularyUuid;
        this.orderIndex = orderIndex;
    }

    static public TermDto fromNamedArea(NamedArea namedArea) {
        TermDto dto = new TermDto(namedArea.getUuid(), namedArea.getRepresentations(), namedArea.getOrderIndex());
        return dto;
    }

    /**
     *
     * @param representation_L10n a blank instance of ITermRepresentation_L10n
     *   created by the  default constructor
     */
    public void localize(ITermRepresentation_L10n representation_L10n) {

        representation_L10n.localize(representations);
        if (representation_L10n.getLabel() != null) {
            setRepresentation_L10n(representation_L10n.getLabel());
        }
        if (representation_L10n.getAbbreviatedLabel() != null) {
            setRepresentation_L10n_abbreviatedLabel(representation_L10n.getAbbreviatedLabel());
        }
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return the representation_L10n
     */
    public String getRepresentation_L10n() {
        return representation_L10n;
    }

    /**
     * @param representation_L10n the representation_L10n to set
     */
    public void setRepresentation_L10n(String representation_L10n) {
        this.representation_L10n = representation_L10n;
    }

    /**
     * @param representation_L10n_abbreviatedLabel the representation_L10n_abbreviatedLabel to set
     */
    public void setRepresentation_L10n_abbreviatedLabel(String representation_L10n_abbreviatedLabel) {
        this.representation_L10n_abbreviatedLabel = representation_L10n_abbreviatedLabel;
    }

    /**
     * @return the representation_L10n_abbreviatedLabel
     */
    public String getRepresentation_L10n_abbreviatedLabel() {
        return representation_L10n_abbreviatedLabel;
    }

    /**
     * @return the vocabularyUuid
     */
    public UUID getVocabularyUuid() {
        return vocabularyUuid;
    }

    /**
     * @param vocabularyUuid the vocabularyUuid to set
     */
    public void setVocabularyUuid(UUID vocabularyUuid) {
        this.vocabularyUuid = vocabularyUuid;
    }

    /**
     * @return the partOfUuid
     */
    public UUID getPartOfUuid() {
        return partOfUuid;
    }

    /**
     * @param representation
     */
    public void addRepresentation(Representation representation) {
        representations.add(representation);

    }

    /**
     * @return the orderIndex
     */
    public Integer getOrderIndex() {
        return orderIndex;
    }

    /**
     * @param orderIndex the orderIndex to set
     */
    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Collection<TermDto> getIncludes() {
        return includes;
    }

    /**
     * @param includes the includes to set
     */
    public void setIncludes(Collection<TermDto> includes) {
        this.includes = includes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TermDto other = (TermDto) obj;
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

}
