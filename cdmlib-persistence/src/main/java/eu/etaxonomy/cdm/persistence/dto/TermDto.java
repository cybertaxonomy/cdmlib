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
public class TermDto extends AbstractTermDto implements Serializable{

    private static final long serialVersionUID = 5627308906985438034L;

    private UUID partOfUuid = null;
    private UUID vocabularyUuid = null;
    private Integer orderIndex = null;
    private Collection<TermDto> includes;

    public TermDto(UUID uuid, Set<Representation> representations, Integer orderIndex) {
        super(uuid, representations);
        this.setOrderIndex(orderIndex);
    }

    public TermDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID vocabularyUuid, Integer orderIndex) {
        super(uuid, representations);
        this.partOfUuid = partOfUuid;
        this.vocabularyUuid = vocabularyUuid;
        this.orderIndex = orderIndex;
    }

    static public TermDto fromNamedArea(NamedArea namedArea) {
        TermDto dto = new TermDto(namedArea.getUuid(), namedArea.getRepresentations(), namedArea.getOrderIndex());
        return dto;
    }

    public UUID getVocabularyUuid() {
        return vocabularyUuid;
    }

    public void setVocabularyUuid(UUID vocabularyUuid) {
        this.vocabularyUuid = vocabularyUuid;
    }

    public UUID getPartOfUuid() {
        return partOfUuid;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Collection<TermDto> getIncludes() {
        return includes;
    }

    public void setIncludes(Collection<TermDto> includes) {
        this.includes = includes;
    }

}
