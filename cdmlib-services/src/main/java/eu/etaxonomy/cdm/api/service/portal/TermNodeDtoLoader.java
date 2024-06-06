/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import eu.etaxonomy.cdm.api.dto.portal.tmp.TermNodeDto;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;

/**
 * @author muellera
 * @since 29.02.2024
 */
public class TermNodeDtoLoader {

    public static TermNodeDtoLoader INSTANCE(){
        return new TermNodeDtoLoader();
    }

    public <T extends DefinedTermBase<T>> TermNodeDto fromEntity(TermNode<T> entity, TermNodeDto parent) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);
        //TODO i18n
        String label = entity.getTerm() == null? "Empty node" : "Node for " + entity.getTerm().getLabel();
        TermNodeDto dto = new TermNodeDto(entity.getUuid(), entity.getId(), label, parent);
        load(dto, entity);
        return dto;
    }

    private <T extends DefinedTermBase<T>> void load(TermNodeDto dto, TermNode<T> entity) {

        dto.setTerm(TermDtoLoader.INSTANCE().fromEntity(entity.getTerm()));
        for (TermNode<?> child : entity.getChildNodes()) {
            dto.addChild(TermNodeDtoLoader.INSTANCE().fromEntity(child, dto));
        }
    }
}
