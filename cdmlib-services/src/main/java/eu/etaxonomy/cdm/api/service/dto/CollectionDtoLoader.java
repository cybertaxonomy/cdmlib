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

import eu.etaxonomy.cdm.api.dto.CollectionDTO;
import eu.etaxonomy.cdm.model.occurrence.Collection;

/**
 * Loader for {@link CollectionDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class CollectionDtoLoader {

    public static CollectionDtoLoader INSTANCE(){
        return new CollectionDtoLoader();
    }

    public CollectionDTO fromEntity(Collection entity) {
        if(entity == null) {
            return null;
        }
        CollectionDTO dto = new CollectionDTO(entity.getUuid(), entity.getTitleCache());
        return load(dto, entity, new HashSet<>());
    }

    private CollectionDTO fromEntity(Collection entity, Set<Collection> collectionsSeen) {
        if(entity == null) {
            return null;
        }
        CollectionDTO dto = new CollectionDTO(entity.getUuid(), entity.getTitleCache());
        return load(dto, entity, collectionsSeen);
    }

    private CollectionDTO load(CollectionDTO dto, Collection collection, Set<Collection> collectionsSeen) {

        dto.setCode(collection.getCode());
        dto.setCodeStandard(collection.getCodeStandard());
        if (collection.getInstitute() != null){
            dto.setInstitute(collection.getInstitute().getTitleCache());
        }
        dto.setTownOrLocation(collection.getTownOrLocation());
        if(collection.getSuperCollection() != null && !collectionsSeen.contains(collection.getSuperCollection())) {
            collectionsSeen.add(collection.getSuperCollection());
            dto.setSuperCollection(fromEntity(collection.getSuperCollection(), collectionsSeen));
        }
        return dto;
    }
}