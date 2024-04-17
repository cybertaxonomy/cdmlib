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

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.config.IAnnotatableLoaderConfiguration;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * @author muellera
 * @since 01.03.2024
 */
public class DistributionDtoLoader {

    public static DistributionDtoLoader INSTANCE(){
        return new DistributionDtoLoader();
    }

    public DistributionDto fromEntity(Distribution entity, IAnnotatableLoaderConfiguration config) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);

        DistributionDto dto = new DistributionDto(entity.getUuid(), entity.getId(),
                TermDtoLoader.INSTANCE().fromEntity(entity.getArea()),
                TermDtoLoader.INSTANCE().fromEntity(entity.getStatus()));
        load(dto, entity, config);
        return dto;
    }

    private <T extends DefinedTermBase<T>> void load(DistributionDto dto, Distribution entity, IAnnotatableLoaderConfiguration config) {
        //copied from TaxonPageDtoLoader
        TaxonPageDtoLoaderBase.loadSources(config, entity, dto);

        //annotatable
        TaxonPageDtoLoaderBase.loadAnnotatable(config, entity, dto);

        dto.setTimeperiod(entity.getTimeperiod() == null ? null : entity.getTimeperiod().toString());
        dto.setDescriptionType(entity.getInDescription() == null? EnumSet.noneOf(DescriptionType.class)
                : entity.getInDescription().getTypes());
    }
}