/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * Wrapping DTO to include all data required by data portal simple search.
 * See #10472 and #10597.
 *
 * @author muellera
 * @since 25.02.2024
 */
public class TaxonFindDto {

    private IdentifiableEntity<?> entity;

    private UUID acceptedTaxonUuid;

    private UUID entityUuid;

    private String sourceString;

    //#10597
    private Set<UUID> classificationUuids = new HashSet<>();

    //#10597
    private List<TaggedText> taxonTaggedText = null;


    public IdentifiableEntity<?> getEntity() {
        return entity;
    }
    public void setEntity(IdentifiableEntity<?> entity) {
        this.entity = entity;
    }


    public UUID getAcceptedTaxonUuid() {
        return acceptedTaxonUuid;
    }
    public void setAcceptedTaxonUuid(UUID acceptedTaxonUuid) {
        this.acceptedTaxonUuid = acceptedTaxonUuid;
    }

    public String getSourceString() {
        return sourceString;
    }
    public void setSourceString(String sourceString) {
        this.sourceString = sourceString;
    }

    public Set<UUID> getClassificationUuids() {
        return classificationUuids;
    }
    public void addClassificationUuid(UUID classificationUuid) {
        this.classificationUuids.add(classificationUuid);
    }

    public List<TaggedText> getTaxonTaggedText() {
        return taxonTaggedText;
    }
    public void setTaxonTaggedText(List<TaggedText> taxonTaggedText) {
        this.taxonTaggedText = taxonTaggedText;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }
    public void setEntityUuid(UUID entityUuid) {
        this.entityUuid = entityUuid;
    }
}