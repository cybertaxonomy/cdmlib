/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.api.dto.MediaDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;

/**
 * Loader for {@link MediaDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class MediaDtoLoader {

    public static MediaDtoLoader INSTANCE() {
        return new MediaDtoLoader();
    }

    /**
     * Creates a list of {@link MediaDTO}s from the Media entity.
     * For each MediaRepresentationPart a single MediaDTO is being created.
     * TODO this needs to be changed so that it is possible to filter the representations by preferences,
     * see {@link MediaUtils#findBestMatchingRepresentation(Media, Class, Integer, Integer, Integer, String[], eu.etaxonomy.cdm.model.media.MediaUtils.MissingValueStrategy)}
     */
    public List<MediaDTO> fromEntity(Media entity) {
        List<MediaDTO> dtos = new ArrayList<>();
        entity.getAllTitles(); // initialize all titles!!!
        @SuppressWarnings("unchecked")
        MediaDTO dto = new MediaDTO((Class<Media>)CdmBase.deproxy(entity).getClass(), entity.getUuid());
        for (MediaRepresentation rep :entity.getRepresentations()){
            for(MediaRepresentationPart p : rep.getParts()){
                if(p.getUri() != null){
                    dto.setUri(p.getUri().toString());
                    break;
                }
            }
        }
        dto.setSources(SourceDtoLoader.fromEntities(entity.getSources()));
        if(dto.getUri() != null || !dto.getSources().isEmpty()) {
            dtos.add(dto);
        }
        return dtos;
    }
}