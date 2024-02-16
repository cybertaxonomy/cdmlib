/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.api.dto.DnaQualityDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.molecular.DnaQuality;

/**
 * @author muellera
 * @since 17.02.2024
 */
public class DnaQualityDtoLoader {

    public static DnaQualityDtoLoader INSTANCE(){
        return new DnaQualityDtoLoader();
    }

    public DnaQualityDTO fromEntity(DnaQuality entity) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);
        DnaQualityDTO dto = new DnaQualityDTO((Class)entity.getClass(), entity.getUuid(), null);
        load(dto, entity);
        return dto;
    }

    private void load(DnaQualityDTO dto, DnaQuality entity) {

        //TODO i18n
        List<Language> lanugages = new ArrayList<>();

        dto.setConcentration(entity.getConcentration());
        dto.setRatioOfAbsorbance260_230(entity.getRatioOfAbsorbance260_230());
        dto.setRatioOfAbsorbance260_280(entity.getRatioOfAbsorbance260_280());

        //unit
        if (entity.getConcentrationUnit() != null) {

            String unit = entity.getConcentrationUnit().getPreferredAbbreviation(lanugages, false, true);
            //TODO is this correct for a not existing unit?
            if (StringUtils.isEmpty(unit)) {
                unit = entity.getConcentrationUnit().getPreferredLabel(lanugages);
            }
            dto.setConcentrationUnit(unit);
        }

        if (entity.getQualityCheckDate() != null) {
            dto.setQualityCheckDate(DtoUtil.fromDateTime(entity.getQualityCheckDate()));
        }

        dto.setPurificationMethod(null);
        if (entity.getQualityTerm() != null) {
            dto.setQualityTerm(entity.getQualityTerm().getPreferredLabel(lanugages));
        }
//        if (entity.getTypedPurificationMethod() != null) {
//            dto.setTypedPurificationMethod(entity.getPurificationMethod()));
//        }
    }
}
