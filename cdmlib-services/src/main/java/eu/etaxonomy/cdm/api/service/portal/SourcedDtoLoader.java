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
import eu.etaxonomy.cdm.model.common.SourcedEntityBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * Bulk loader for all {@link SourcedEntityBase sourced entities}.
 *
 * @author muellera
 * @since 07.03.2024
 */
public class SourcedDtoLoader {

    public static SourcedDtoLoader INSTANCE(){
        return new SourcedDtoLoader();
    }

    /**
     * DTOs must have id initialized
     */
    public void loadAll(Set<SourcedDto> dtos, Class baseClass, ICdmGenericDao commonDao,
            EnumSet<OriginalSourceType> sourceTypes, ProxyDtoLoader lazyLoader) {

        Set<Integer> baseIds = dtos.stream().map(d->d.getId()).collect(Collectors.toSet());

        SetMap<Integer,SourcedDto> id2SourcedInstancesMap = new SetMap<>(); //it is a set because there might be multiple instances for the same object
        dtos.stream().forEach(dto->id2SourcedInstancesMap.putItem(dto.getId(), dto));

        String hql = "SELECT new map(bc.id as baseId, s.id as sourceId) "
                + " FROM "+baseClass.getSimpleName()+" bc JOIN bc.sources s "
                + " WHERE s.type IN :osbTypes AND bc.id IN :baseIds";

        Map<String,Object> params = new HashMap<>();
        params.put("osbTypes", sourceTypes);
        params.put("baseIds", baseIds);

        List<Map<String, Integer>> sourceIdMapping;
        try {
            sourceIdMapping = commonDao.getHqlMapResult(hql, params, Integer.class);

            sourceIdMapping.stream().forEach(e->{
                Integer sourceId = e.get("sourceId");
                SourceDto sourceDto = new SourceDto(sourceId);
                Integer baseId = e.get("baseId");
                id2SourcedInstancesMap.get(baseId).stream().forEach(sdd->sdd.addSource(sourceDto));
                lazyLoader.add(OriginalSourceBase.class, sourceDto);
            });

            if (baseClass.equals(DescriptionElementBase.class)) {
                loadIndescriptionAll(dtos, baseClass, commonDao, sourceTypes, lazyLoader, baseIds, id2SourcedInstancesMap);
            }
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("Exception while loading sources for sourced entities", e);
        }
    }

    private void loadIndescriptionAll(Set<SourcedDto> dtos, Class baseClass, ICdmGenericDao commonDao,
            EnumSet<OriginalSourceType> sourceTypes, ProxyDtoLoader lazyLoader, Set<Integer> baseIds, SetMap<Integer, SourcedDto> id2SourcedInstancesMap) {

        String hql = "SELECT new map(deb.id as baseId, s.id as sourceId) "
                + " FROM "+baseClass.getSimpleName()+" deb "
                        + " JOIN deb.inDescription d JOIN d.sources s "
                + " WHERE s.type IN :osbTypes AND deb.id IN :baseIds";

        Map<String,Object> params = new HashMap<>();
        params.put("osbTypes", sourceTypes);
        params.put("baseIds", baseIds);

        List<Map<String, Integer>> sourceIdMapping;
        try {
            sourceIdMapping = commonDao.getHqlMapResult(hql, params, Integer.class);

            sourceIdMapping.stream().forEach(e->{
                Integer sourceId = e.get("sourceId");
                SourceDto sourceDto = new SourceDto(sourceId);
                Integer baseId = e.get("baseId");
                id2SourcedInstancesMap.get(baseId).stream().forEach(sdd->sdd.addSource(sourceDto));
                lazyLoader.add(OriginalSourceBase.class, sourceDto);
            });

        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("Exception while loading sources for sourced entities", e);
        }
    }
}