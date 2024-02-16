/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.api.dto.DerivedUnitDTO;
import eu.etaxonomy.cdm.api.dto.MediaDTO;
import eu.etaxonomy.cdm.api.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.api.dto.SpecimenTypeDesignationDTO;
import eu.etaxonomy.cdm.api.dto.compare.DeterminationEventDtoComparator;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * Loader for {@link SpecimenOrObservationBaseDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public abstract class SpecimenOrObservationBaseDtoLoader<DTO extends SpecimenOrObservationBaseDTO> {

//    protected SpecimenOrObservationBaseDTO(Class<SpecimenOrObservationBase<?>> type, UUID uuid, String label) {
//        super(type, uuid, label);
//        xx;
//    }

    protected void load(
            SpecimenOrObservationBase<?> specimenOrObservation,
            DTO dto) {

        //old
//        super(HibernateProxyHelper.getClassWithoutInitializingProxy(specimenOrObservation), specimenOrObservation.getUuid(), specimenOrObservation.getTitleCache());

        dto.setId(specimenOrObservation.getId());  //TODO sollen wir das auf public lassen?

        Set<Media> collectedMedia = collectMedia(specimenOrObservation);
        addMediaAsDTO(dto, collectedMedia);
        dto.setKindOfUnit(specimenOrObservation.getKindOfUnit());
        dto.setSex(specimenOrObservation.getSex());
        dto.setIndividualCount(specimenOrObservation.getIndividualCount());
        dto.setLifeStage(specimenOrObservation.getLifeStage());
        FieldUnit fieldUnit = null;
        if (specimenOrObservation instanceof FieldUnit){
            fieldUnit = (FieldUnit)specimenOrObservation;
        }else{
            fieldUnit = getFieldUnit((DerivedUnit)specimenOrObservation);
        }
        if (fieldUnit != null){
            AgentBase<?> collector = null;
            if (fieldUnit.getGatheringEvent() != null){
                collector = fieldUnit.getGatheringEvent().getCollector();
            }
            String fieldNumberString = CdmUtils.Nz(fieldUnit.getFieldNumber());
            String collectorsString = null;
            if (collector != null){
                if (collector.isInstanceOf(TeamOrPersonBase.class)){
                    collectorsString = CdmBase.deproxy(collector, TeamOrPersonBase.class).getCollectorTitleCache();
                }else{
                    collectorsString = collector.getTitleCache();  //institutions
                }
            }
            collectorsString = CdmUtils.concat(" ", collectorsString, fieldNumberString);
            dto.setCollectorsString(collectorsString);
        }
        dto.setDeterminations(specimenOrObservation.getDeterminations().stream()
                .map(det -> DeterminationEventDtoLoader.fromEntity(det))
                .collect(Collectors.toList())
                );
        Collections.sort(dto.getDeterminations(), new DeterminationEventDtoComparator());

        if (specimenOrObservation instanceof DerivedUnit){
            DerivedUnit derivedUnit = (DerivedUnit)specimenOrObservation;
            if (derivedUnit.getSpecimenTypeDesignations() != null){
                setSpecimenTypeDesignations(dto, derivedUnit.getSpecimenTypeDesignations());
            }
        }
    }

    /**
     * finds the field unit of the derived unit or null if no field unit exist
     * @param specimenOrObservation
     * @return
     */
    private FieldUnit getFieldUnit(DerivedUnit specimenOrObservation) {
        if (specimenOrObservation.getDerivedFrom() != null && !specimenOrObservation.getDerivedFrom().getOriginals().isEmpty()){
            for (SpecimenOrObservationBase<?> specimen: specimenOrObservation.getDerivedFrom().getOriginals()){
                if (specimen instanceof FieldUnit){
                    return (FieldUnit)specimen;
                }else if (specimen instanceof DerivedUnit){
                    getFieldUnit(HibernateProxyHelper.deproxy(specimen,DerivedUnit.class));
                }
            }
        }
        return null;
    }

    protected Set<Media> collectMedia(SpecimenOrObservationBase<?> specimenOrObservation){
        Set<Media> collectedMedia = new HashSet<>();
        Set<SpecimenDescription> descriptions = specimenOrObservation.getSpecimenDescriptionImageGallery();
        for (DescriptionBase<?> desc : descriptions){
            if (desc instanceof SpecimenDescription){
                SpecimenDescription specimenDesc = (SpecimenDescription)desc;
                for (DescriptionElementBase element : specimenDesc.getElements()){
                    if (element.isInstanceOf(TextData.class)&& element.getFeature().equals(Feature.IMAGE())) {
                        for (Media media :element.getMedia()){
                            collectedMedia.add(media);
                        }
                    }
                }
            }
        }
        return collectedMedia;
    }

    private void addMediaAsDTO(SpecimenOrObservationBaseDTO dto, Set<Media> media) {
        for(Media m : media) {
            List<MediaDTO> dtos = MediaDtoLoader.fromEntity(m);
            dto.getListOfMedia().addAll(dtos);
        }
    }

    /**
     * @param sob
     *      The Unit to assemble the derivatives information for
     * @param maxDepth
     *   The maximum number of derivation events levels up to which derivatives are to be assembled.
     *   <code>NULL</code> means infinitely.
     * @param includeTypes
     *      Allows for positive filtering by {@link SpecimenOrObservationType}.
     *      Filter is disabled when <code>NULL</code>. This only affects the derivatives assembled in the
     *      {@link #derivatives} list. The <code>unitLabelsByCollection</code> are always collected for the
     *      whole bouquet of derivatives.
     * @param unitLabelsByCollection
     *      A map to record the unit labels (most significant identifier + collection code) per collection.
     *      Optional parameter, may be <code>NULL</code>.
     * @return
     */
    protected Set<DerivedUnitDTO> assembleDerivatives(SpecimenOrObservationBaseDTO<?> dto,
            SpecimenOrObservationBase<?> sob,
            Integer maxDepth, EnumSet<SpecimenOrObservationType> includeTypes) {

        boolean doDescend = maxDepth == null || maxDepth > 0;
        Integer nextLevelMaxDepth = maxDepth != null ? maxDepth - 1 : null;
        Set<DerivedUnitDTO> derivateDTOs = new HashSet<>();
        // collectDerivedUnitsMaxdepth => 0 to avoid aggregation of sub ordinate
        // derivatives at each level
        Integer collectDerivedUnitsMaxdepth = 0;
        for (DerivedUnit derivedUnit : sob.collectDerivedUnits(collectDerivedUnitsMaxdepth)) {
            if(!derivedUnit.isPublish()){
                continue;
            }

            if (doDescend && (includeTypes == null || includeTypes.contains(derivedUnit.getRecordBasis()))) {
                SpecimenOrObservationBaseDTO<?> derivedUnitDTO = SpecimenOrObservationDTOFactory.fromEntity(derivedUnit, nextLevelMaxDepth);
                if (derivedUnitDTO instanceof DerivedUnitDTO) {
                    derivateDTOs.add((DerivedUnitDTO)derivedUnitDTO);
                }
            }
        }
        return derivateDTOs;
    }

    protected void setSpecimenTypeDesignations(DTO dto, Set<SpecimenTypeDesignation> specimenTypeDesignations) {

        for (SpecimenTypeDesignation typeDes: specimenTypeDesignations){
            if (typeDes != null){
                SpecimenTypeDesignationDTO typeDto = SpecimenTypeDesignationDtoLoader.fromEntity(typeDes);
                dto.addSpecimenTypeDesignation(typeDto);
            }
        }
    }
}