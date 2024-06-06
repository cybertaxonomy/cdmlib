/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.api.dto.PrimerDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.molecular.Primer;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class PrimerDtoLoader {

    public static PrimerDtoLoader INSTANCE(){
        return new PrimerDtoLoader();
    }

    public PrimerDTO fromEntity(Primer entity) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);
        PrimerDTO dto = new PrimerDTO((Class)entity.getClass(), entity.getUuid(), entity.getLabel());
        load(dto, entity);
        return dto;
    }

    private void load(PrimerDTO dto, Primer entity) {
        dto.setDnaMarker(DefinedTermDtoLoader.INSTANCE().fromEntity(entity.getDnaMarker()));
        dto.setPublishedIn(ReferenceDtoLoader.fromEntity(entity.getPublishedIn()));
        if (entity.getSequence() != null) {
            dto.setSequence(entity.getSequence().getString());
        }
    }
}