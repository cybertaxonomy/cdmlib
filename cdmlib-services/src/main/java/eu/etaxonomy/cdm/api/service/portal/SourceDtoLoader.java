/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.api.dto.SourceDTO;
import eu.etaxonomy.cdm.api.dto.portal.SourceDto;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * Loader for {@link SourceDto}
 *
 * Note: be aware that there is currently still the {@link eu.etaxonomy.cdm.api.service.dto.SourceDtoLoader}
 * class for loading {@link SourceDTO}. This should be merged or better distinguished in future.
 *
 * @author muellera
 * @since 12.03.2024
 */
public class SourceDtoLoader {

    public static SourceDtoLoader INSTANCE(){
        return new SourceDtoLoader();
    }


    /**
     * DTOs must have id initialized
     */
    public void loadAll(Set<SourceDto> dtos, Class baseClass, ICdmGenericDao commonDao,
            EnumSet<OriginalSourceType> sourceTypes, LazyDtoLoader lazyLoader) {

        Set<Integer> baseIds = dtos.stream().map(d->d.getId()).collect(Collectors.toSet());

        SetMap<Integer,SourceDto> dtosForSource = new SetMap<>();
        dtos.stream().forEach(dto->dtosForSource.putItem(dto.getId(), dto));


        String hql = "SELECT new map(osb.id as id, osb.uuid as uuid, osb.accessed as accessed, "
                +     " osb.originalInfo originalInfo, "
                +     " osb.citation ref, osb.citationMicroReference detail) "
                //cdmSource, type, links
                + " FROM OriginalSourceBase osb "
                + " WHERE osb.id IN :baseIds"
                ;

        Map<String,Object> params = new HashMap<>();
//        params.put("osbTypes", sourceTypes);
        params.put("baseIds", baseIds);

        try {
            List<Map<String, Object>> sourceMap = commonDao.getHqlMapResult(hql, params, Object.class);

            sourceMap.stream().forEach(e->{
                //TODO existiert schon
                Integer id = (Integer)e.get("sourceId");

                dtosForSource.get(id).stream().forEach(dto->{
                    dto.setAccessed(null);  //TODO timeperiod
                    dto.setDoi(null); //TODO doi
                    dto.setLastUpdated(null); //TODO
                    dto.setOriginalInfo(null);
                    dto.setUuid((UUID)e.get("uuid"));
                    dto.setCitationDetail((String)e.get("detail"));
                });
//                lazyLoader.add(OriginalSourceBase.class, sourceDto);
            });
        } catch (UnsupportedOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
