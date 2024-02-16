/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.EnumSet;

import eu.etaxonomy.cdm.api.dto.DerivationTreeSummaryDTO;
import eu.etaxonomy.cdm.api.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 * Loader for {@link FieldUnitDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class FieldUnitDtoLoader extends SpecimenOrObservationBaseDtoLoader<FieldUnitDTO> {

    public static FieldUnitDtoLoader INSTANCE() {
        return new FieldUnitDtoLoader();
    }

    public FieldUnitDTO fromEntity(FieldUnit entity){
        return fromEntity(entity, null, null);
    }

    /**
     * Factory method for the construction of a FieldUnitDTO.
     * <p>
     * The direct derivatives are added to the field {@link #getDerivatives() derivates}.
     *
     *
     * @param fieldUnit
     *     The FieldUnit entity to create a DTO for. Is null save.
     * @param maxDepth
     *     The max number of levels to walk into the derivation tree, <code>null</code> means unlimited.
     * @param typeIncludeFilter
     *     Set of SpecimenOrObservationType to be included into the collection of {@link #getDerivatives() derivative DTOs}
     */
    public FieldUnitDTO fromEntity(FieldUnit entity, Integer maxDepth, EnumSet<SpecimenOrObservationType> typeIncludeFilter){
        if(entity == null) {
            return null;
        }
        FieldUnitDTO dto = new FieldUnitDTO(FieldUnit.class, entity.getUuid(), entity.getTitleCache());

        return load(dto, entity, maxDepth, typeIncludeFilter);
    }

    /**
     * The direct derivatives are added to the field {@link #getDerivatives() derivates}.
     *
     * @param fieldUnit
     *     The FieldUnit entity to create a DTO for
     * @param maxDepth
     *   The maximum number of derivation events levels up to which derivatives are to be collected.
     *   <code>null</code> means infinitely.
     * @param typeIncludeFilter
     *     Set of SpecimenOrObservationType to be included into the collection of {@link #getDerivatives() derivative DTOs}
     */
    private FieldUnitDTO load(FieldUnitDTO dto, FieldUnit fieldUnit, Integer maxDepth,
            EnumSet<SpecimenOrObservationType> typeIncludeFilter ) {

        super.load(fieldUnit, dto);

        dto.setFieldNotes(fieldUnit.getFieldNotes());
        dto.setFieldNumber(fieldUnit.getFieldNumber());
        if(typeIncludeFilter == null) {
            typeIncludeFilter = EnumSet.allOf(SpecimenOrObservationType.class);
        }
        if (fieldUnit.getGatheringEvent() != null){
            dto.setGatheringEvent(GatheringEventDtoLoader.fromEntity(fieldUnit.getGatheringEvent()));
        }
        dto.setRecordBase(fieldUnit.getRecordBasis());

        // --------------------------------------

        if (fieldUnit.getGatheringEvent() != null) {
            GatheringEvent gatheringEvent = fieldUnit.getGatheringEvent();
            // Country
            NamedArea country = gatheringEvent.getCountry();
            dto.setCountry(country != null ? country.getLabel() : null);
            // Collection
            AgentBase<?> collector = gatheringEvent.getCollector();
            String fieldNumber = fieldUnit.getFieldNumber();
            String collectionString = "";
            if (collector != null || fieldNumber != null) {
                collectionString += collector != null ? collector : "";
                if (!collectionString.isEmpty()) {
                    collectionString += " ";
                }
                collectionString += (fieldNumber != null ? fieldNumber : "");
                collectionString = collectionString.trim();
            }
            dto.setCollectingString(collectionString);
            dto.setDate(gatheringEvent.getGatheringDate());
        }

        // assemble derivate data DTO
        DerivationTreeSummaryDTO derivateDataDTO = DerivationTreeSummaryDtoLoader.fromEntity(fieldUnit, null);
        dto.setDerivationTreeSummary(derivateDataDTO);

        // assemble citation
        String summaryLabel = fieldUnit.getTitleCache();
        if((CdmUtils.isBlank(summaryLabel) || summaryLabel.equals(IdentifiableEntityDefaultCacheStrategy.TITLE_CACHE_GENERATION_NOT_IMPLEMENTED))
                && !fieldUnit.isProtectedTitleCache()){
            fieldUnit.setTitleCache(null);
            summaryLabel = fieldUnit.getTitleCache();
        }

        dto.addAllDerivatives(assembleDerivatives(dto, fieldUnit, maxDepth, typeIncludeFilter));

        return dto;
    }
}