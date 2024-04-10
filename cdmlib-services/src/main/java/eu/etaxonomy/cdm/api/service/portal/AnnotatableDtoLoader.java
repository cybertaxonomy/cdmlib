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
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * @author muellera
 * @since 07.03.2024
 */
public class AnnotatableDtoLoader {

    public static AnnotatableDtoLoader INSTANCE(){
        return new AnnotatableDtoLoader();
    }

    /**
     * DTOs must have id initialized
     */
    public void loadAll(Set<AnnotatableDto> dtos, Class baseClass, ICdmGenericDao commonDao,
            Set<UUID> annotationTypeFilter, LazyDtoLoader lazyLoader) {

        Set<Integer> baseIds = dtos.stream().map(d->d.getId()).collect(Collectors.toSet());
        SetMap<Integer,AnnotatableDto> dtosForAnnotatable = new SetMap<>();
        dtos.stream().forEach(dto->dtosForAnnotatable.putItem(dto.getId(), dto));

        String hql = "SELECT new map(bc.id as id, a.id as annotationId) "
                + " FROM "+baseClass.getSimpleName()+" bc JOIN bc.annotations a "
                //TODO allow empty type filter
                + " WHERE a.annotationType.uuid IN :annotationTypes AND bc.id IN :baseIds";

        Map<String,Object> params = new HashMap<>();
        params.put("annotationTypes", annotationTypeFilter);
        params.put("baseIds", baseIds);

        List<Map<String, Integer>> annotationIdMapping;
        try {
            annotationIdMapping = commonDao.getHqlMapResult(hql, params, Integer.class);

            annotationIdMapping.stream().forEach(e->{
                Integer annotationId = e.get("annotationId");
                AnnotationDto annotationDto = new AnnotationDto();
                annotationDto.setId(annotationId);
                Integer annotatableId = e.get("id");
                dtosForAnnotatable.get(annotatableId).stream().forEach(a->a.addAnnotation(annotationDto));
                lazyLoader.add(Annotation.class, annotationDto);
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
