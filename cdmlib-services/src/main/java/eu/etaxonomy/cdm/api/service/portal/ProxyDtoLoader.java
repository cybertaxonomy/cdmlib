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
import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.api.dto.portal.AnnotatableDto;
import eu.etaxonomy.cdm.api.dto.portal.AnnotationDto;
import eu.etaxonomy.cdm.api.dto.portal.CdmBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDtoBase;
import eu.etaxonomy.cdm.api.dto.portal.MediaDto2;
import eu.etaxonomy.cdm.api.dto.portal.SourceDto;
import eu.etaxonomy.cdm.api.dto.portal.SourcedDto;
import eu.etaxonomy.cdm.api.dto.portal.config.ISourceableLoaderConfiguration;
import eu.etaxonomy.cdm.api.service.dto.AnnotationDtoLoader;
import eu.etaxonomy.cdm.api.service.dto.MediaDto2Loader;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * Stores DTO proxies (loaded only with id) and loads them
 * on demand by calling the appropriate loader.
 *
 * @author muellera
 * @since 07.03.2024
 */
public class ProxyDtoLoader {

    private SetMap<Class<? extends CdmBase>,AnnotatableDto> annotatableProxies = new SetMap<>();

    private SetMap<Class<? extends CdmBase>,SourcedDto> sourcedProxies = new SetMap<>();

//    private SetMap<Class<? extends CdmBase>,IdentifiableDto> identifiableProxies = new SetMap<>();

    private Set<FactDtoBase> factProxies = new HashSet<>();

    private Set<SourceDto> sourceProxies = new HashSet<>();

    private Set<AnnotationDto> annotationProxies = new HashSet<>();

    private Set<MediaDto2> mediaProxies = new HashSet<>();


    private TaxonFactsDtoLoader factsLoader;

    public ProxyDtoLoader(TaxonFactsDtoLoader factsLoader) {
        this.factsLoader = factsLoader;
    }

    public <T extends CdmBaseDto> T add(Class<? extends CdmBase> clazz, T dto) {

        if (dto instanceof AnnotatableDto) {
            annotatableProxies.putItem(clazz, (AnnotatableDto)dto);
        }
        if (dto instanceof SourcedDto) {
            sourcedProxies.putItem(clazz, (SourcedDto)dto);
        }
        if (dto instanceof FactDtoBase) {
            factProxies.add((FactDtoBase)dto);
        }
//        if (dto instanceof IdentifiableDto) {
//            identifiableProxies.putItem(clazz, (IdentifiableDto)dto);
//        }
        if(dto instanceof SourceDto) {
            sourceProxies.add((SourceDto)dto);
        } else if(dto instanceof AnnotationDto) {
            annotationProxies.add((AnnotationDto)dto);
        } else if (dto instanceof MediaDto2) {
            mediaProxies.add((MediaDto2)dto);
        }

        return dto;
    }

    public void loadAll(ICdmGenericDao dao, EnumSet<OriginalSourceType> sourceTypes,
            ISourceableLoaderConfiguration config) {

        while (hasUnloaded()) {

            Set<FactDtoBase> factDtos = new HashSet<>(factProxies);
            factProxies.clear();
            factsLoader.loadProxyFacts(factDtos);

            Set<MediaDto2> mediaDto2s = new HashSet<>(mediaProxies);
            mediaProxies.clear();
            MediaDto2Loader.INSTANCE().loadAll(mediaDto2s, dao);

            SetMap<Class<? extends CdmBase>,SourcedDto> sourcedProxiesClone = new SetMap<>();
            sourcedProxies.entrySet().forEach(e->sourcedProxiesClone.put(e.getKey(), e.getValue()));
            sourcedProxies.clear();
            for (Class<? extends CdmBase> clazz : sourcedProxiesClone.keySet()) {
                Set<SourcedDto> sourcedDto = new HashSet<>(sourcedProxiesClone.get(clazz));
                SourcedDtoLoader.INSTANCE().loadAll(sourcedDto,
                        clazz, dao, sourceTypes, this);
            }

            SetMap<Class<? extends CdmBase>,AnnotatableDto> annotatableProxiesClone = new SetMap<>();
            annotatableProxies.entrySet().forEach(e->annotatableProxiesClone.put(e.getKey(), e.getValue()));
            annotatableProxies.clear();
            for (Class<? extends CdmBase> clazz : annotatableProxiesClone.keySet()) {
                Set<AnnotatableDto> anDto = new HashSet<>(annotatableProxiesClone.get(clazz));
                AnnotatableDtoLoader.INSTANCE().loadAll(anDto,
                        clazz, dao, config, this);
            }

            Set<SourceDto> sourceDtos = new HashSet<>(sourceProxies);
            sourceProxies.clear();
            SourceDtoLoader.INSTANCE().loadAll(sourceDtos,
                    dao, sourceTypes, this);

            Set<AnnotationDto> annotationDtos = new HashSet<>(annotationProxies);
            annotationProxies.clear();
            AnnotationDtoLoader.INSTANCE().loadAll(annotationDtos, dao);

//            if (!identifiableProxies.isEmpty()) {
//                System.out.println("ERROR removing identifiableProxies!!!");
//            }
//            //FIXME only for testing
//            identifiableProxies.clear();
        }
    }

    private boolean hasUnloaded() {
        return ! (annotatableProxies.isEmpty()
                && annotationProxies.isEmpty()
                && factProxies.isEmpty()
//                && identifiableProxies.isEmpty()
                && mediaProxies.isEmpty()
                && sourcedProxies.isEmpty()
                && sourceProxies.isEmpty())
                ;
    }
}