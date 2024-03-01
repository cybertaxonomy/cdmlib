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
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.api.dto.portal.SourceDto;
import eu.etaxonomy.cdm.api.dto.portal.SourcedDto;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * @author muellera
 * @since 07.03.2024
 */
public class SourcedDtoLoader {

    public static SourcedDtoLoader INSTANCE(){
        return new SourcedDtoLoader();
    }

    //TODO config sourceTypes

    /**
     * DTOs must have id initialized
     */
    public void loadAll(Set<SourcedDto> dtos, Class baseClass, ICdmGenericDao commonDao,
            EnumSet<OriginalSourceType> sourceTypes, LazyDtoLoader lazyLoader) {

        Set<Integer> baseIds = dtos.stream().map(d->d.getId()).collect(Collectors.toSet());
        SetMap<Integer,SourcedDto> dtosForSource = new SetMap<>();
        dtos.stream().forEach(dto->dtosForSource.putItem(dto.getId(), dto));

        String hql = "SELECT new map(bc.id as factId, s.id as sourceId) "
                + " FROM "+baseClass.getSimpleName()+" bc JOIN bc.sources s "
                + " WHERE s.type IN :osbTypes AND bc.id IN :baseIds";

        Map<String,Object> params = new HashMap<>();
        params.put("osbTypes", sourceTypes);
        params.put("baseIds", baseIds);

        List<Map<String, Integer>> sourceIdMapping;
        try {
            sourceIdMapping = commonDao.getHqlMapResult(hql, params, Integer.class);

            sourceIdMapping.stream().forEach(e->{
                SourceDto sourceDto = new SourceDto(e.get("sourceId"));
                Integer factId = e.get("factId");
                dtosForSource.get(factId).stream().forEach(sdd->sdd.addSource(sourceDto));
                lazyLoader.add(OriginalSourceBase.class, sourceDto);
            });
        } catch (UnsupportedOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //
//  //load supplemental data
//  String hqlSourcesMap = "SELECT new map(deb.id as factId, s.id as sourceId) "
//          + " FROM DescriptionElementBase deb JOIN deb.sources s "
//          + " WHERE s.type IN :osbTypes AND deb.id IN :factIds";
//  Object[] params = new Object[] {config.getSourceTypes()};
//  List<Map<String,Integer>> sourceIdMapping = dao.getHqlMapResult(hqlSourcesMap, params, Integer.class);
//  sourceIdMapping.stream().forEach(e->{
//      SourceDto sourceDto = new SourceDto(e.get("sourceId"));
//      AllFactTypesDto fact = factsForSource.get(e.get("factid"));
////    //TODO add source to fact
//  });
}
