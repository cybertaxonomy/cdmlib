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

import eu.etaxonomy.cdm.api.dto.AmplificationResultDTO;
import eu.etaxonomy.cdm.api.dto.MediaDTO;
import eu.etaxonomy.cdm.api.dto.SingleReadDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.SingleRead;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class AmplificationResultDtoLoader {


    public static AmplificationResultDtoLoader INSTANCE(){
        return new AmplificationResultDtoLoader();
    }

    public AmplificationResultDTO fromEntity(AmplificationResult entity) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);
        AmplificationResultDTO dto = new AmplificationResultDTO((Class)entity.getClass(), entity.getUuid(), null);
        load(dto, entity);
        return dto;
    }

    private void load(AmplificationResultDTO dto, AmplificationResult entity) {

        //TODO EventBase params

        if (entity.getAmplification() != null) {
            Amplification amplification = entity.getAmplification();

            dto.setDnaMarker(DefinedTermDtoLoader.INSTANCE().fromEntity(amplification.getDnaMarker()));
            dto.setElectrophoresisVoltage(amplification.getElectrophoresisVoltage());
            dto.setForwardPrimer(PrimerDtoLoader.INSTANCE().fromEntity(amplification.getForwardPrimer()));
            dto.setReversePrimer(PrimerDtoLoader.INSTANCE().fromEntity(amplification.getReversePrimer()));
            dto.setGelConcentration(amplification.getGelConcentration());
            dto.setGelRunningTime(amplification.getGelRunningTime());

            if (amplification.getInstitution() != null) {
                dto.setInstitution(amplification.getInstitution().getTitleCache());
            }

            dto.setLadderUsed(amplification.getLadderUsed());

            if (amplification.getPurification() != null) {
                if (amplification.getPurification().getDefinedMaterialOrMethod() != null) {
                    //TODO i18n
                    dto.setPurification(amplification.getPurification().getDefinedMaterialOrMethod().getPreferredLabel(null));
                } else {
                    dto.setPurification(amplification.getPurification().getDescription());
                }
            }
        }
        if (entity.getGelPhoto() != null) {
            List<MediaDTO> mediaDtos = MediaDtoLoader.fromEntity(entity.getGelPhoto());
            if (mediaDtos != null && !mediaDtos.isEmpty()) {
                //TODO filter best fit
                dto.setGelPhoto(mediaDtos.get(0));
            }
        }

        dto.setSuccessful(entity.getSuccessful());
        dto.setSuccessText(entity.getSuccessText());

        for (SingleRead singleRead : entity.getSingleReads()) {
            SingleReadDTO singleReadDto = SingleReadDtoLoader.INSTANCE().fromEntity(singleRead);
            dto.addSingleRead(singleReadDto);
        }
    }

}
