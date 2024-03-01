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
import java.util.Map;

import eu.etaxonomy.cdm.api.dto.portal.AnnotatableDto;
import eu.etaxonomy.cdm.api.dto.portal.CdmBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDto;
import eu.etaxonomy.cdm.api.dto.portal.IdentifiableDto;
import eu.etaxonomy.cdm.api.dto.portal.SourcedDto;
import eu.etaxonomy.cdm.api.service.dto.SourceDtoLoader;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * Stores DTO proxies (loaded only with id) and loads them
 * on demand by calling the appropriate loader.
 *
 * @author muellera
 * @since 07.03.2024
 */
public class LazyDtoLoader {

    private SetMap<Class<? extends CdmBase>,AnnotatableDto> annotatableProxies = new SetMap<>();

    private SetMap<Class<? extends CdmBase>,SourcedDto> sourcedProxies = new SetMap<>();

    private SetMap<Class<? extends CdmBase>,IdentifiableDto> identifiableProxies = new SetMap<>();

    //not necessarily a map
    private Map<Class<DescriptionElementBase>,? extends FactDto> factProxies = new HashMap<>();

    private Map<Class<OriginalSourceBase>,? extends FactDto> sourceProxies = new HashMap<>();



    public <T extends CdmBaseDto> T add(Class<? extends CdmBase> clazz, T dto) {
        if (dto instanceof AnnotatableDto) {
            annotatableProxies.putItem(clazz, (AnnotatableDto)dto);
        }
        if (dto instanceof SourcedDto) {
            sourcedProxies.putItem(clazz, (SourcedDto)dto);
        }
        if (dto instanceof IdentifiableDto) {
            identifiableProxies.putItem(clazz, (IdentifiableDto)dto);
        }
        return dto;
    }

    public void loadAll(ICdmGenericDao dao, EnumSet<OriginalSourceType> sourceTypes) {

        for (Class<? extends CdmBase> clazz : sourcedProxies.keySet()) {
            SourcedDtoLoader.INSTANCE().loadAll(sourcedProxies.get(clazz),
                    clazz, dao, sourceTypes, this);
        }

        //TODO clazz distinction needed here
        for (Class<? extends CdmBase> clazz : sourceProxies.keySet()) {
            SourceDtoLoader .INSTANCE().loadAll(sourcedProxies.get(clazz),
                    clazz, dao, sourceTypes, this);
        }


    }
}