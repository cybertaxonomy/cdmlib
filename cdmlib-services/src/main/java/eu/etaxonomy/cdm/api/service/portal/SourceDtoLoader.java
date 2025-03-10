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

import eu.etaxonomy.cdm.api.dto.SourceDTO;
import eu.etaxonomy.cdm.api.dto.portal.SourceDto;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

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
    public void loadAll(Set<SourceDto> dtos, ICdmGenericDao commonDao,
            ProxyDtoLoader lazyLoader) {

        Set<Integer> baseIds = dtos.stream().map(d->d.getId()).collect(Collectors.toSet());

        SetMap<Integer,SourceDto> dtosForSource = new SetMap<>();
        dtos.stream().forEach(dto->dtosForSource.putItem(dto.getId(), dto));


        String hql = "SELECT new map(osb.class as cdmClass, "
                +     " osb.id as id, osb.uuid as uuid, osb.accessed as accessed, "
                +     " osb.originalInfo as originalInfo, "
                +     " ref as ref, osb.citationMicroReference as detail, "
                +     " osb.type as type, osb.accessed as accessed,"
                +     " nameInSource as nameInSource) "
                //cdmSource, links
                //TODO can we avoid outer join?
                + " FROM OriginalSourceBase osb LEFT JOIN osb.nameUsedInSource nameInSource "
                + "      LEFT JOIN osb.citation ref"
                + " WHERE osb.id IN :baseIds"
                ;

        Map<String,Object> params = new HashMap<>();

        params.put("baseIds", baseIds);

        try {
            List<Map<String, Object>> sourceMap = commonDao.getHqlMapResult(hql, params, Object.class);

            sourceMap.stream().forEach(e->{
                Integer id = (Integer)e.get("id");

                dtosForSource.get(id).stream().forEach(dto->{
                    //uuid
                    UUID uuid = (UUID)e.get("uuid");
                    dto.setUuid(uuid);
                    //type
                    OriginalSourceType type = (OriginalSourceType)e.get("type");
                    dto.setType(type != null ? type.toString() : null);
                    //accessed
                    TimePeriod accessed = (TimePeriod)e.get("accessed");
                    dto.setAccessed(accessed == null ? null : accessed.toString());
                    //originalInfo
                    dto.setOriginalInfo((String)e.get("originalInfo"));
                    //detail
                    String detail = (String)e.get("detail");
                    dto.setCitationDetail(detail);
                    //ref
                    //TODO use DTO for citation
                    Reference citation = (Reference)e.get("ref");
                    if (citation != null) {
                        dto.setReferenceUuid(citation.getUuid());
                        //doi
                        dto.setDoi(citation.getDoiUriString());
                        //uri
                        dto.setUri(citation.getUri());
                        //sortableDate
                        dto.setSortableDate(citation.getSortableDateString());
                    }
                    String label = OriginalSourceFormatter.INSTANCE_LONG_CITATION.format(citation, detail, accessed);
                    String clazz = (String)e.get("cdmClass");
                    TypedLabel typedLabel = new TypedLabel(uuid, clazz, label);
                    dto.addLabel(typedLabel);

                    ICdmBase linkedObject = citation;
                    if (linkedObject == null) {
                        //cdmsource
                        //TODO
//                        linkedObject = source.getCdmSource();
                        //TODO specimen references
                    }
                    dto.setLinkedUuid(linkedObject == null ? null : linkedObject.getUuid());
                    String linkedObjectStr = linkedObject == null ? null : CdmBase.deproxy(linkedObject).getClass().getSimpleName();
                    dto.setLinkedClass(linkedObjectStr);

                    //nameUsedInSource
                    //TODO use DTO
                    TaxonName name = (TaxonName)e.get("nameInSource");
                    if (name != null) {
                        List<TaggedText> taggedName = name.cacheStrategy().getTaggedTitle(name);
                        //TODO nom status?
                        dto.setNameInSource(taggedName);
                        dto.setNameInSourceUuid(name.getUuid());
                    }

                    //links
                    dto.addLink(null); //TODO

                    //last updated
                    dto.setLastUpdated(null); //TODO
                });
//                lazyLoader.add(OriginalSourceBase.class, sourceDto);
            });
        } catch (Exception e) {
//          e.printStackTrace();
            throw e;
        }
    }
}
