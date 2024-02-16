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

import eu.etaxonomy.cdm.api.dto.SourceDTO;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;

/**
 * Loader for {@link SourceDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class SourceDtoLoader {

    public static Set<SourceDTO> fromEntities(Set<? extends OriginalSourceBase> entities){
        Set<SourceDTO> refDtos = new HashSet<>();
        //TODO allow filtering
        entities.stream().forEach(s->refDtos.add(SourceDtoLoader.fromEntity(s)));
        return refDtos;
    }

    public static SourceDTO fromEntity(OriginalSourceBase entity) {
        //TODO name used in source not needed?
        if(entity == null) {
            return null;
        }
        SourceDTO dto = new SourceDTO();
        dto.setUuid(entity.getUuid());
        dto.setLabel(OriginalSourceFormatter.INSTANCE.format(entity));
        dto.setCitation(ReferenceDtoLoader.fromEntity(entity.getCitation()));
        dto.setCitationDetail(entity.getCitationMicroReference());
        return dto;
    }
}
