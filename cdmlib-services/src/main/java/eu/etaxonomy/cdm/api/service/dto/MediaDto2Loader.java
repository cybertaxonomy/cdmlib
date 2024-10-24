/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.api.dto.portal.MediaDto2;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.MediaRepresentationDTO;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * Loader for {@link MediaDto2}s.
 *
 * @author muellera
 * @since 12.04.2024
 */
public class MediaDto2Loader {

    public static MediaDto2Loader INSTANCE() {
        return new MediaDto2Loader();
    }

    //TODO merge both MediaDTOs
    public <M extends  MediaDto2>
        void loadAll(Set<M> mediaDtos, ICdmGenericDao dao) {

        Set<Integer> baseIds = mediaDtos.stream().map(d->d.getId()).collect(Collectors.toSet());

        SetMap<Integer,M> id2MediaMap = new SetMap<>();
        mediaDtos.stream().forEach(dto->id2MediaMap.putItem(dto.getId(), dto));

        String hql = "SELECT new map(m.id as mediaId, m.uuid as uuid, m.titleCache as label, "
                +     " m.artist as artist, "
                +     " r.id as repId, r.uuid as repUuid, r.mimeType as mimeType, "
                +     " r.suffix as suffix, p.uri as uri,  p.size as size, "
                +     " type(p) as clazz, "  //TODO #10582 throws exception if mediaRepresentationPart is missing
                +     " p.height as heigth, p.width as width) "
                + " FROM Media m "
                + "      LEFT JOIN m.representations r "
                + "      LEFT JOIN r.mediaRepresentationParts p "
                + "      LEFT JOIN m.artist artist "
                + " WHERE m.id IN :baseIds "
                + " ORDER BY m.id, r.id, p.id "
                ;

        Map<String,Object> params = new HashMap<>();
        params.put("baseIds", baseIds);

        try {
            List<Map<String, Object>> mediaMap = dao.getHqlMapResult(hql, params, Object.class);

            mediaMap.stream().forEach(e->{
                Integer id = (Integer)e.get("mediaId");

                id2MediaMap.get(id).stream().forEach(dto->{
                    dto.setUuid((UUID)e.get("uuid"));
                    dto.setLabel((String)e.get("label"));
                    //"deduplication" takes place automatically as we simply override media data
                    //   and use add() for the representation
                    MediaRepresentationDTO repDto = new MediaRepresentationDTO();
                    repDto.setId((Integer)e.get("repId"));
                    repDto.setUuid((UUID)e.get("repUuid"));
                    repDto.setClazz(((Class)e.get("clazz")).getSimpleName()); //TODO Simple name
                    repDto.setHeight((Integer)e.get("heigth"));
                    repDto.setWidth((Integer)e.get("width"));
                    repDto.setSize((Integer)e.get("size"));
                    repDto.setSuffix((String)e.get("suffix"));
                    repDto.setMimeType((String)e.get("mimeType"));
                    repDto.setUri((URI)e.get("uri"));
                    dto.addRepresentation(repDto);
                    dto.setLastUpdated(null); //TODO
                });
            });
        } catch (Exception e) {
            throw new RuntimeException("Exception while loading annotation data", e);
        }
        return;
    }
}