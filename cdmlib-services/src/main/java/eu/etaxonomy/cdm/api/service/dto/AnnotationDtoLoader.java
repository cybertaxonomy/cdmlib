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

import eu.etaxonomy.cdm.api.dto.AnnotationDTO;
import eu.etaxonomy.cdm.api.dto.portal.AnnotationDto;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * Loader for {@link AnnotationDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class AnnotationDtoLoader {

    public static AnnotationDtoLoader INSTANCE(){
        return new AnnotationDtoLoader();
    }

    public <T extends CdmBase> AnnotationDTO fromEntity(Annotation annotation) {
        @SuppressWarnings("unchecked")
        AnnotationDTO dto = new AnnotationDTO((Class<Annotation>)annotation.getClass(), annotation.getUuid());
        if(annotation.getAnnotationType() != null) {
            dto.setAnnotationTypeUuid(annotation.getAnnotationType().getUuid());
        }
        dto.setText(annotation.getText());
        return dto;
    }

    /**
     * DTOs must have id initialized
     */
    public void loadAll(Set<AnnotationDto> dtos, ICdmGenericDao commonDao) {

        Set<Integer> baseIds = dtos.stream().map(d->d.getId()).collect(Collectors.toSet());

        SetMap<Integer,AnnotationDto> id2AnnotationMap = new SetMap<>();
        dtos.stream().forEach(dto->id2AnnotationMap.putItem(dto.getId(), dto));


        String hql = "SELECT new map(a.id as id, a.uuid as uuid, "
                +     " a.text as text, at.uuid as typeUuid) "
                + " FROM Annotation a LEFT JOIN a.annotationType at "
                + " WHERE a.id IN :baseIds "
                ;

        Map<String,Object> params = new HashMap<>();
        params.put("baseIds", baseIds);

        try {
            List<Map<String, Object>> annotationMap = commonDao.getHqlMapResult(hql, params, Object.class);

            annotationMap.stream().forEach(e->{
                Integer id = (Integer)e.get("id");

                id2AnnotationMap.get(id).stream().forEach(dto->{
                    UUID uuid = (UUID)e.get("uuid");
                    dto.setUuid(uuid);
                    dto.setText((String)e.get("text"));
                    dto.setTypeUuid((UUID)e.get("typeUuid"));
                    dto.setLastUpdated(null); //TODO
                });
            });
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("Exception while loading annotation data", e);
        }
        return;
    }
}