/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.net.URISyntaxException;
import java.util.List;

import eu.etaxonomy.cdm.api.dto.MediaDTO;
import eu.etaxonomy.cdm.api.dto.SequenceDTO;
import eu.etaxonomy.cdm.model.molecular.Sequence;

/**
 * Loader for {@link SequenceDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 14.02.2024
 */
public class SequenceDtoLoader {

    public static SequenceDTO fromEntity(Sequence sequence) {
        //TODO inherit from TypeReference
        SequenceDTO dto = new SequenceDTO();
        load(dto, sequence);
        return dto;
    }

    private static void load(SequenceDTO dto, Sequence seq){

        if (seq.getContigFile() != null) {
            List<MediaDTO> mediaDtos = MediaDtoLoader.fromEntity(seq.getContigFile());
            if (mediaDtos != null && !mediaDtos.isEmpty()) {
                //TODO filter best fit
                dto.setContigFile(mediaDtos.get(0));
            }
        }

        dto.setConsensusSequence(seq.getConsensusSequence() == null ? null : seq.getConsensusSequence().getString());
        dto.setIsBarcode(seq.getIsBarcode());
        dto.setBarcodeSequencePart(seq.getBarcodeSequencePart() == null ? null : seq.getBarcodeSequencePart().getString());
        dto.setGeneticAccessionNumber(seq.getGeneticAccessionNumber());
        dto.setBoldProcessId(seq.getBoldProcessId());

        dto.setSingleReadAlignments(seq.getSingleReadAlignments());

        if (seq.getDnaMarker() != null){
            dto.setDnaMarker(seq.getDnaMarker().getLabel());
        }
        dto.setHaplotype(seq.getHaplotype());

        dto.setCitations(ReferenceDtoLoader.fromEntities(seq.getCitations()));
        try{
            dto.setBoldUri(seq.getBoldUri());
            dto.setDdbjUri(seq.getDdbjUri());
            dto.setEmblUri(seq.getEmblUri());
            dto.setGenBankUri(seq.getGenBankUri());
        } catch (URISyntaxException e){
//TODO
        }
    }

}
