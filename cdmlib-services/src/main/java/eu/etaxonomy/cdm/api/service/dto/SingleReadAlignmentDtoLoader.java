/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.api.dto.SingleReadAlignmentDTO;
import eu.etaxonomy.cdm.api.dto.SingleReadDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment;

/**
 *  Loader for {@link SingleReadAlignmentDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 21.11.2024
 */
public class SingleReadAlignmentDtoLoader {

    public static SingleReadAlignmentDtoLoader INSTANCE() {
        return new SingleReadAlignmentDtoLoader();
    }

    public SingleReadAlignmentDTO fromEntity(SingleReadAlignment entity) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);
        SingleReadAlignmentDTO dto = new SingleReadAlignmentDTO((Class)entity.getClass(), entity.getUuid(), null);
        load(dto, entity);
        return dto;
    }

    private static void load(SingleReadAlignmentDTO dto, SingleReadAlignment alignment) {

        if (alignment.getSingleRead() != null) {
            SingleReadDTO singleReadDto = SingleReadDtoLoader.INSTANCE().fromEntity(alignment.getSingleRead());
            dto.setSingleRead(singleReadDto);
        }

        dto.setEditedSequence(alignment.getEditedSequence());
        dto.setFirstSeqPosition(alignment.getFirstSeqPosition());
        dto.setLeftCutPosition(alignment.getLeftCutPosition());
        dto.setReverseComplement(alignment.isReverseComplement());
        dto.setRightCutPosition(alignment.getRightCutPosition());
    }
}