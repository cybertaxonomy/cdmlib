/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.api.dto.AnnotationDTO;
import eu.etaxonomy.cdm.api.dto.DNASampleDTO;
import eu.etaxonomy.cdm.api.dto.SequenceDTO;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;

/**
 * Loader for {@link AnnotationDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 14.02.2024
 */
public class DnaSampleDtoLoader extends DerivedUnitDtoLoaderBase<DnaSample> {

    public static DnaSampleDtoLoader INSTANCE() {
        return new DnaSampleDtoLoader();
    }

    @Override
    public DNASampleDTO fromEntity(DnaSample entity){
        if(entity == null) {
            return null;
        }
        DNASampleDTO dto = new DNASampleDTO(DnaSample.class, entity.getUuid(), entity.getTitleCache());

        return load(dto, CdmBase.deproxy(entity));
    }

    private DNASampleDTO load(DNASampleDTO dto, DerivedUnit derivedUnit) {

        super.load(dto, derivedUnit);

        DnaSample dnaSample = HibernateProxyHelper.deproxy(derivedUnit, DnaSample.class);
        Set<SequenceDTO> seqDtos = new HashSet<>();
        for (Sequence seq: dnaSample.getSequences()){
            seqDtos.add(SequenceDtoLoader.fromEntity(seq));
        }
        dto.setSequences(seqDtos);
        for (AmplificationResult amplificationResult : dnaSample.getAmplificationResults()) {
              dto.addAmplificationResult(AmplificationResultDtoLoader.INSTANCE().fromEntity(amplificationResult));
        }
        dto.setDnaQuality(DnaQualityDtoLoader.INSTANCE().fromEntity(dnaSample.getDnaQuality()));
        return dto;
    }
}