/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.stream.Collectors;

import eu.etaxonomy.cdm.api.dto.DeterminationEventDTO;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.ref.TaggedEntityReference;

/**
 * Loader for {@link DeterminationEventDTO}s. Extracted from the DTO.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class DeterminationEventDtoLoader {

    public static DeterminationEventDTO fromEntity(DeterminationEvent entity) {
        if(entity == null) {
            return null;
        }
        DeterminationEventDTO dto = new DeterminationEventDTO(entity);
        if(entity.getTaxon() != null) {
            dto.setDetermination(new TaggedEntityReference<>(Taxon.class, entity.getTaxon().getUuid(), entity.getTaxon().getTaggedTitle()));
        } else if(entity.getTaxonName() != null) {
            dto.setDetermination(new TaggedEntityReference<>(TaxonName.class, entity.getTaxonName().getUuid(), entity.getTaxonName().getTaggedName()));
        }
        dto.setModifier(entity.getModifier());
        dto.setPreferred(entity.getPreferredFlag());
        if(!entity.getReferences().isEmpty()) {
            dto.setReferences(
                    entity.getReferences().stream()
                        .map(r -> ReferenceDtoLoader.fromEntity(r))
                        .collect(Collectors.toSet()));
        }
        return dto;
    }
}
