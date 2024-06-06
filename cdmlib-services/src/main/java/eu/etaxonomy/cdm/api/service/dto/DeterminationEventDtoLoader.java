/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.List;
import java.util.Set;
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
public class DeterminationEventDtoLoader extends EventDtoLoaderBase {

    public static DeterminationEventDtoLoader INSTANCE(){
        return new DeterminationEventDtoLoader();
    }

    public List<DeterminationEventDTO> fromEntities(Set<DeterminationEvent> entities){
        return entities.stream()
            .map(det -> fromEntity(det))
            .collect(Collectors.toList());

    }

    public DeterminationEventDTO fromEntity(DeterminationEvent entity) {
        if(entity == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        DeterminationEventDTO dto = new DeterminationEventDTO((Class<DeterminationEvent>)entity.getClass(), entity.getUuid());
        load(dto, entity);
        return dto;
    }

    private void load(DeterminationEventDTO dto, DeterminationEvent entity) {
        super.load(dto, entity);
        if(entity.getTaxon() != null) {
            @SuppressWarnings("unchecked")
            TaggedEntityReference<Taxon> taxonDto = TaggedEntityReference.from(Taxon.class, entity.getTaxon().getUuid(), entity.getTaxon().getTaggedTitle());
            dto.setDetermination(taxonDto);
        } else if(entity.getTaxonName() != null) {
            dto.setDetermination(TaggedEntityReference.from(TaxonName.class, entity.getTaxonName().getUuid(), entity.getTaxonName().getTaggedName()));
        }
        dto.setModifier(DefinedTermDtoLoader.INSTANCE().fromEntity(entity.getModifier()));
        dto.setPreferred(entity.getPreferredFlag());
        if(!entity.getReferences().isEmpty()) {
            dto.setReferences(
                    entity.getReferences().stream()
                        .map(r -> ReferenceDtoLoader.fromEntity(r))
                        .collect(Collectors.toSet()));
        }
    }

}
