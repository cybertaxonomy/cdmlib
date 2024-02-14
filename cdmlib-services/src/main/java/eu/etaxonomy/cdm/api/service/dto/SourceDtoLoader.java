/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.api.dto.SourceDTO;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;

/**
 * Loader for {@link SourceDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class SourceDtoLoader {

    //TODO rename to formNamedSource
    public static SourceDTO fromDescriptionElementSource(NamedSourceBase entity) {
        //TODO name used in source not needed?
        SourceDTO dto = fromSourceBase(entity);
        if(entity != null) {
            dto.setLabel(OriginalSourceFormatter.INSTANCE.format(entity));
        }
        return dto;
    }

    public static SourceDTO fromIdentifiableSource(IdentifiableSource entity) {
        SourceDTO dto = fromSourceBase(entity);
        if(entity != null) {
            //TODO AM unclear why IdentifiableSource does not have the label set
        }
        return dto;
    }

    private static SourceDTO fromSourceBase(OriginalSourceBase entity) {
        if(entity == null) {
            return null;
        }
        SourceDTO dto = new SourceDTO();
        dto.setUuid(entity.getUuid());
        dto.setLabel(OriginalSourceFormatter.INSTANCE.format(entity));
        dto.setCitation(ReferenceDtoLoader.fromEntity(entity.getCitation()));
        dto.setCitationDetail(entity.getCitationMicroReference());
        return dto;
    }
}
