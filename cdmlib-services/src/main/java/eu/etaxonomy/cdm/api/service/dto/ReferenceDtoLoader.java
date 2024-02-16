/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.api.dto.ReferenceDTO;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Loader for the {@link ReferenceDTO}.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class ReferenceDtoLoader {

    public static ReferenceDTO fromEntity(Reference entity) {
        if(entity == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        ReferenceDTO dto = new ReferenceDTO((Class<Reference>)entity.getClass(), entity.getUuid(), entity.getTitleCache());
        //TODO see ReferenceDTO.titleCache
        dto.setTitleCache(entity.getTitleCache());
        dto.setAbbrevTitleCache(entity.getAbbrevTitleCache());
        dto.setUri(entity.getUri());
        dto.setDoi(entity.getDoi());
        dto.setDatePublished(entity.getDatePublished());

        return dto;
    }

    public static Set<ReferenceDTO> fromEntities(Set<Reference> entities){
        Set<ReferenceDTO> refDtos = new HashSet<>();
        //TODO allow filtering
        entities.stream().forEach(r->refDtos.add(ReferenceDtoLoader.fromEntity(r)));
        return refDtos;
    }
}