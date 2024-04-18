/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.api.dto.portal.AnnotatableDto;
import eu.etaxonomy.cdm.api.dto.portal.AnnotationDto;
import eu.etaxonomy.cdm.api.dto.portal.MarkerDto;
import eu.etaxonomy.cdm.api.dto.portal.config.IAnnotatableLoaderConfiguration;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * Bulk loader for all {@link AnnotatableEntity annotatable entities}.
 *
 * @author muellera
 * @since 10.04.2024
 */
public class AnnotatableDtoLoader {

    public static AnnotatableDtoLoader INSTANCE(){
        return new AnnotatableDtoLoader();
    }

    /**
     * DTOs must have id initialized
     */
    public void loadAll(Set<AnnotatableDto> dtos, Class baseClass, ICdmGenericDao commonDao,
            IAnnotatableLoaderConfiguration config, ProxyDtoLoader lazyLoader) {

        Set<Integer> baseIds = dtos.stream().map(d->d.getId()).collect(Collectors.toSet());

        SetMap<Integer,AnnotatableDto> id2AnnotatableInstancesMap = new SetMap<>(); //it is a set because there might be multiple instances for the same object
        dtos.stream().forEach(dto->id2AnnotatableInstancesMap.putItem(dto.getId(), dto));

        handleAnnotations(baseClass, commonDao, config, lazyLoader, baseIds, id2AnnotatableInstancesMap);
        //TODO not yet used as a marker loader does not exist yet
        //handleMarkers(baseClass, commonDao, markerTypeFilter, lazyLoader, baseIds, id2AnnotatableInstancesMap);
    }

    private void handleAnnotations(Class baseClass, ICdmGenericDao commonDao,
            IAnnotatableLoaderConfiguration config, ProxyDtoLoader lazyLoader,
            Set<Integer> baseIds,
            SetMap<Integer,AnnotatableDto> id2AnnotatableInstancesMap) {

        Map<String,Object> params = new HashMap<>();
        String hql = "SELECT new map(bc.id as id, a.id as annotationId) "
                + " FROM "+baseClass.getSimpleName()+" bc JOIN bc.annotations a "
                + " WHERE bc.id IN :baseIds";
        params.put("baseIds", baseIds);
        if (config.getAnnotationTypes() != null) {
            hql += " AND a.annotationType.uuid IN :annotationTypes ";
            params.put("annotationTypes", config.getAnnotationTypes());
        }

        try {
            List<Map<String, Integer>> baseId2annotationIdMapping = commonDao.getHqlMapResult(hql, params, Integer.class);

            baseId2annotationIdMapping.stream().forEach(e->{
                Integer annotationId = e.get("annotationId");
                AnnotationDto annotationDto = new AnnotationDto();
                annotationDto.setId(annotationId);
                Integer annotatableId = e.get("id");
                id2AnnotatableInstancesMap.get(annotatableId).stream().forEach(a->a.addAnnotation(annotationDto));
                lazyLoader.add(Annotation.class, annotationDto);
            });
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("Exception while loading supplemental data for annotatable entities", e);
        }
    }

    private void handleMarkers(Class baseClass, ICdmGenericDao commonDao, Set<UUID> markerTypeFilter,
            ProxyDtoLoader lazyLoader, Set<Integer> baseIds,
            SetMap<Integer, AnnotatableDto> id2AnnotatableInstancesMap) {

        Map<String,Object> params = new HashMap<>();
        String hql = "SELECT new map(bc.id as baseId, m.id as markerId) "
                + " FROM "+baseClass.getSimpleName()+" bc JOIN bc.markers m "
                + " WHERE bc.id IN :baseIds";
        params.put("baseIds", baseIds);

        if (markerTypeFilter != null) {
            hql += " AND m.markerType.uuid IN :markerTypes ";
            params.put("markerTypes", markerTypeFilter);
        }

        try {
            List<Map<String, Integer>> baseId2markerIdMapping = commonDao.getHqlMapResult(hql, params, Integer.class);

            baseId2markerIdMapping.stream().forEach(e->{
                Integer markerId = e.get("markerId");
                MarkerDto markerDto = new MarkerDto();
                markerDto.setId(markerId);
                Integer baseId = e.get("baseId");
                id2AnnotatableInstancesMap.get(baseId).stream().forEach(m->m.addMarker(markerDto));
                lazyLoader.add(Marker.class, markerDto);
            });
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("Exception while loading supplemental data for annotatable entities", e);
        }
    }
}