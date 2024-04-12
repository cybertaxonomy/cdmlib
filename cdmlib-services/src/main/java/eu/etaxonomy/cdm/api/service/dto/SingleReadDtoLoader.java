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

import eu.etaxonomy.cdm.api.dto.MediaDTO;
import eu.etaxonomy.cdm.api.dto.SingleReadDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.occurrence.MaterialOrMethodEvent;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class SingleReadDtoLoader {

    public static SingleReadDtoLoader INSTANCE(){
        return new SingleReadDtoLoader();
    }

    public SingleReadDTO fromEntity(SingleRead entity) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);
        SingleReadDTO dto = new SingleReadDTO((Class)entity.getClass(), entity.getUuid(), null);
        load(dto, entity);
        return dto;
    }

    private void load(SingleReadDTO dto, SingleRead entity) {

        if (entity.getDirection() != null) {
            dto.setDirection(entity.getDirection().getLabel());
        }
        if (entity.getMaterialOrMethod() != null) {
            MaterialOrMethodEvent matMeth = entity.getMaterialOrMethod();
            if (matMeth.getDefinedMaterialOrMethod() != null) {
                //TODO i18n
                dto.setMaterialOrMethod(matMeth.getDefinedMaterialOrMethod().getPreferredLabel(null));
            }else {
                dto.setMaterialOrMethod(matMeth.getMaterialMethodText());
            }
        }

        if (entity.getPherogram() != null) {
            List<MediaDTO> mediaDtos = MediaDtoLoader.INSTANCE().fromEntity(entity.getPherogram());
            if (mediaDtos != null && !mediaDtos.isEmpty()) {
                //TODO filter best fit
                dto.setPherogram(mediaDtos.get(0));
            }
        }
        dto.setPrimer(PrimerDtoLoader.INSTANCE().fromEntity(entity.getPrimer()));
        dto.setSequence(entity.getSequenceString());
    }
}
