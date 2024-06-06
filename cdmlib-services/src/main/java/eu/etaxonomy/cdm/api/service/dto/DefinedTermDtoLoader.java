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

import eu.etaxonomy.cdm.api.dto.DefinedTermDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * Loader for {@link DefinedTermDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 16.02.2024
 */
public class DefinedTermDtoLoader {

    public static DefinedTermDtoLoader INSTANCE(){
        return new DefinedTermDtoLoader();
    }

    public <T extends CdmBase> DefinedTermDTO fromEntity(DefinedTermBase<?> entity) {
        return fromEntity(entity, null);
    }

    //TODO use locales
    public <T extends CdmBase> DefinedTermDTO fromEntity(DefinedTermBase<?> entity, List<Language> languages) {
        if (entity == null) {
            return null;
        } else {
            entity = CdmBase.deproxy(entity);
        }

        String label = entity.getPreferredLabel(null);
        DefinedTermDTO dto = new DefinedTermDTO(entity.getClass(), entity.getUuid(), label);

        return dto;
    }
}
