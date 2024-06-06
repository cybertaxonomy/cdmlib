/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.List;

import eu.etaxonomy.cdm.api.dto.DerivationEventDTO;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;

/**
 * Loader for {@link DerivationEventDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 16.02.2024
 */
public class DerivationEventDtoLoader extends EventDtoLoaderBase {

    public static DerivationEventDtoLoader INSTANCE(){
        return new DerivationEventDtoLoader();
    }

    public DerivationEventDTO fromEntity(DerivationEvent entity) {
        if (entity != null) {
            entity = HibernateProxyHelper.deproxy(entity);
            @SuppressWarnings("unchecked")
            DerivationEventDTO dto = new DerivationEventDTO((Class<DerivationEvent>)entity.getClass(), entity.getUuid() );
            load(dto, entity);
            return dto;
        } else {
            return null;
        }
    }

    private void load(DerivationEventDTO dto, DerivationEvent entity) {
        super.load(dto, entity);

        if(entity.getType() != null) {
            List<Language> languages = null; //TODO i18n
            dto.setEventType(entity.getType().getPreferredLabel(languages));
        }

        if(entity.getInstitution() != null) {
            dto.setInstitute(entity.getInstitution().getTitleCache());
        }
    }
}
