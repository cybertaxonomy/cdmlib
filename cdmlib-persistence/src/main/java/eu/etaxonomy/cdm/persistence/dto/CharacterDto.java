/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.CdmClass;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
/**
 * @author k.luther
 * @since Oct 6, 2020
 */
public class CharacterDto extends FeatureDto {

    private static final long serialVersionUID = 1743178749047550590L;

    private TermNodeDto structure;
    private TermDto structureModifier;
    private TermNodeDto property;
    private TermDto propertyModifier;
    private TermNodeDto ratioTo;

    public CharacterDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary, //Set<Representation> vocRepresentations,
            boolean isAvailableForTaxon, boolean isAvailableForTaxonName, boolean isAvailableForOccurrence,
            String titleCache, TermNodeDto structure, TermDto structureModifier, TermNodeDto property,
            TermDto propertyModifier, TermNodeDto ratioTo, boolean isSupportCategoricalData, boolean isSupportsQuantitativeData, Set<TermVocabularyDto> supportedCategoricalEnumerations, Set<TermVocabularyDto> recommendedModifierEnumeration,  Set<TermDto> recommendedMeasurementUnits,  Set<TermDto> recommendedStatisticalMeasures) {
        super(uuid, representations, partOfUuid, kindOfUuid, vocabularyUuid, orderIndex, idInVocabulary,
                isAvailableForTaxon, isAvailableForTaxonName, isAvailableForOccurrence, titleCache, isSupportCategoricalData, isSupportsQuantitativeData, supportedCategoricalEnumerations, recommendedModifierEnumeration, recommendedMeasurementUnits, recommendedStatisticalMeasures);
        this.structure = structure;
        this.structureModifier = structureModifier;
        this.property = property;
        this.propertyModifier = propertyModifier;
        this.setRatioTo(ratioTo);
        this.setTermType(TermType.Character);
    }

    public static CharacterDto fromCharacter(Character character) {
       TermVocabulary voc = character.getVocabulary();

       Set<TermVocabularyDto> recommendedModifierDtos = new HashSet<>();
       for (TermVocabulary<DefinedTerm> modVoc: character.getRecommendedModifierEnumeration()){
           recommendedModifierDtos.add(TermVocabularyDto.fromVocabulary(modVoc));
       }

       Set<TermDto> recommendedStatisticalMeasuresDtos = new HashSet<>();
       for (StatisticalMeasure term: character.getRecommendedStatisticalMeasures()){
           recommendedStatisticalMeasuresDtos.add(TermDto.fromTerm(term));
       }

       Set<TermVocabularyDto> supportedCategoricalDtos = new HashSet<>();
       for (TermVocabulary<State> catVoc: character.getSupportedCategoricalEnumerations()){
           supportedCategoricalDtos.add(TermVocabularyDto.fromVocabulary(catVoc));
       }

       Set<TermDto> recommendedMeasurementUnitsDtos = new HashSet<>();
       for (MeasurementUnit term: character.getRecommendedMeasurementUnits()){
           recommendedMeasurementUnitsDtos.add(TermDto.fromTerm(term));
       }
       CharacterDto dto = new CharacterDto(character.getUuid(), character.getRepresentations(), character.getPartOf() != null? character.getPartOf().getUuid(): null, character.getKindOf() != null? character.getKindOf().getUuid(): null,
               voc != null? voc.getUuid(): null, null, character.getIdInVocabulary(), character.isAvailableForTaxon(), character.isAvailableForTaxonName(), character.isAvailableForOccurrence(), character.getTitleCache(),
                       character.getStructure() !=null? TermNodeDto.fromNode(character.getStructure(), null): null, character.getStructureModifier() != null? TermDto.fromTerm(character.getStructureModifier()): null,
                                       character.getProperty() != null? TermNodeDto.fromNode(character.getProperty(), null): null, character.getPropertyModifier() != null? TermDto.fromTerm(character.getPropertyModifier()): null, character.getRatioToStructure() != null? TermNodeDto.fromNode(character.getRatioToStructure(), null): null,
                                               character.isSupportsCategoricalData(), character.isSupportsQuantitativeData(),supportedCategoricalDtos, recommendedModifierDtos, recommendedMeasurementUnitsDtos, recommendedStatisticalMeasuresDtos);

       return dto;
    }

    public TermNodeDto getStructure() {
        return structure;
    }
    public void setStructure(TermNodeDto structure) {
        this.structure = structure;
    }

    public TermNodeDto getRatioTo() {
        return ratioTo;
    }

    public void setRatioTo(TermNodeDto ratioTo) {
        this.ratioTo = ratioTo;
    }

    public TermDto getStructureModifier() {
        return structureModifier;
    }
    public void setStructureModifier(TermDto structureModifier) {
        this.structureModifier = structureModifier;
    }

    public TermNodeDto getProperty() {
        return property;
    }
    public void setProperty(TermNodeDto property) {
        this.property = property;
    }

    public TermDto getPropertyModifier() {
        return propertyModifier;
    }
    public void setPropertyModifier(TermDto propertyModifier) {
        this.propertyModifier = propertyModifier;
    }


    public static String getTermDtoSelect(){
        String[] result = createSqlParts("Character");

        return result[0]+result[1]+result[2];
    }

    private static String[] createSqlParts(String fromTable) {
                String sqlSelectString = ""
                + "select a.uuid, "
                + "r, "
                + "p.uuid, "
                + "k.uuid, "
                + "v.uuid, "
//                + "a.orderIndex, "
                + "a.idInVocabulary, "
//                + "voc_rep,  "
                + "a.termType,  "
                + "a.uri,  "
                + "m,  "
                + "a.availableFor, "
                + "a.titleCache, "
                + "a.supportedDataTypes, "
                + "recommendedModifierEnumeration, "
                + "recommendedStatisticalMeasures, "
                + "supportedCategoricalEnumerations, "
                + "recommendedMeasurementUnits, "
                + "a.property, "
////                + "a.propertyModifier ";
                + "a.structure, "
//                + "a.structureModifier";
                + "ratioToStructure ";

        String sqlFromString =   " from "+fromTable+" as a ";

        String sqlJoinString =  "LEFT JOIN a.partOf as p "
                + "LEFT JOIN a.kindOf as k "
                + "LEFT JOIN a.media as m "
                + "LEFT JOIN a.representations AS r "
                + "LEFT JOIN a.vocabulary as v "
                + "LEFT JOIN v.representations as voc_rep "
                + "LEFT JOIN a.recommendedModifierEnumeration as recommendedModifierEnumeration "
                + "LEFT JOIN a.recommendedStatisticalMeasures as recommendedStatisticalMeasures "
                + "LEFT JOIN a.supportedCategoricalEnumerations as supportedCategoricalEnumerations "
                + "LEFT JOIN a.recommendedMeasurementUnits as recommendedMeasurementUnits "
                + "LEFT JOIN a.ratioToStructure as ratioToStructure"
                ;

        String[] result = new String[3];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        return result;
    }

    public static List<TermDto> termDtoListFrom(List<Object[]> results) {
        List<TermDto> dtos = new ArrayList<>(); // list to ensure order
        // map to handle multiple representations/media/vocRepresentation because of LEFT JOIN
        Map<UUID, TermDto> dtoMap = new HashMap<>(results.size());
        for (Object[] elements : results) {
            UUID uuid = (UUID)elements[0];
            if(dtoMap.containsKey(uuid)){
                // multiple results for one term -> multiple (voc) representation/media
                if(elements[1]!=null){
                    dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
                }
//                if(elements[6]!=null){
//                    dtoMap.get(uuid).addVocRepresentation((Representation)elements[6]);
//                }
                if(elements[8]!=null){
                    dtoMap.get(uuid).addMedia(((Media) elements[8]).getUuid());
                }
            } else {
                // term representation
                Set<Representation> representations = new HashSet<>();
                if(elements[1] instanceof Representation) {
                    representations = new HashSet<Representation>(1);
                    representations.add((Representation)elements[1]);
                }
                // term media
                Set<UUID> mediaUuids = new HashSet<>();
                if(elements[8] instanceof Media) {
                    mediaUuids.add(((Media) elements[8]).getUuid());
                }
                // voc representation
//                Set<Representation> vocRepresentations = new HashSet<>();
//                if(elements[6] instanceof Representation) {
//                    vocRepresentations = new HashSet<Representation>(7);
//                    vocRepresentations.add((Representation)elements[6]);
//                }
                boolean isAvailableForTaxon = false;
                boolean isAvailableForTaxonName = false;
                boolean isAvailableForOccurrence = false;

                EnumSet<CdmClass> availableForString = (EnumSet<CdmClass>)elements[9];

                if (availableForString.contains(CdmClass.TAXON)){
                    isAvailableForTaxon = true;
                }
                if (availableForString.contains(CdmClass.TAXON_NAME)){
                    isAvailableForTaxonName = true;
                }
                if (availableForString.contains(CdmClass.OCCURRENCE)){
                    isAvailableForOccurrence = true;
                }
                boolean isSupportsCategoricalData = false;
                boolean isSupportsQuantitativeData = false;

                EnumSet<CdmClass> supportsString = (EnumSet<CdmClass>)elements[11];

                if (supportsString.contains(CdmClass.CATEGORICAL_DATA)){
                    isSupportsCategoricalData = true;
                }
                if (supportsString.contains(CdmClass.QUANTITATIVE_DATA)){
                    isSupportsQuantitativeData = true;
                }

                Object o = elements[12];
                Set<TermVocabularyDto> recommendedModifierDtos = new HashSet<>();
                if (o instanceof TermVocabulary){
                    recommendedModifierDtos.add(TermVocabularyDto.fromVocabulary((TermVocabulary)o));
                }else if (o instanceof Set){
                    Set<TermVocabulary<DefinedTerm>> recommendedModifierEnumeration = (Set<TermVocabulary<DefinedTerm>>) o;
                    if (recommendedModifierEnumeration != null){
                        for (TermVocabulary<DefinedTerm> voc: recommendedModifierEnumeration){
                            recommendedModifierDtos.add(TermVocabularyDto.fromVocabulary(voc));
                        }
                    }
                }



                o = elements[13];
                Set<TermDto> recommendedStatisticalMeasuresDtos = new HashSet<>();
                if (o instanceof StatisticalMeasure){
                    recommendedStatisticalMeasuresDtos.add(TermDto.fromTerm((StatisticalMeasure)o));
                }else if (o instanceof Set){
                    Set<StatisticalMeasure> recommendedStatisticalMeasures = (Set<StatisticalMeasure>) o;
                    if (recommendedStatisticalMeasures != null) {
                        for (StatisticalMeasure term: recommendedStatisticalMeasures){
                            recommendedStatisticalMeasuresDtos.add(TermDto.fromTerm(term));
                        }
                    }
                }
                o =  elements[14];
                Set<TermVocabularyDto> supportedCategoricalDtos = new HashSet<>();
                if (o instanceof TermVocabulary){
                    supportedCategoricalDtos.add(TermVocabularyDto.fromVocabulary((TermVocabulary)o));
                }else if (o instanceof Set){
                    Set<TermVocabulary> supportedCategoricalEnumerations = (Set<TermVocabulary>)o;
                    for (TermVocabulary<State> voc: supportedCategoricalEnumerations){
                        supportedCategoricalDtos.add(TermVocabularyDto.fromVocabulary(voc));
                    }
                }


                o = elements[15];
                Set<TermDto> recommendedMeasurementUnitsDtos = new HashSet<>();
                if (o instanceof MeasurementUnit){
                    recommendedMeasurementUnitsDtos.add(TermDto.fromTerm((MeasurementUnit)o));
                }else if (o instanceof Set){
                    Set<MeasurementUnit> recommendedMeasurementUnits = (Set<MeasurementUnit>) elements[15];
                    for (MeasurementUnit term: recommendedMeasurementUnits){
                        recommendedMeasurementUnitsDtos.add(TermDto.fromTerm(term));
                    }
                }

                o = elements[16];
                TermNodeDto prop = null;
                if (o instanceof TermNode){
                    prop = TermNodeDto.fromNode((TermNode) o, TermTreeDto.fromTree((TermTree) ((TermNode)o).getGraph()));
                }

                o = elements[17];
                TermNodeDto structure = null;
                if (o instanceof TermNode){
                    structure = TermNodeDto.fromNode((TermNode) o, TermTreeDto.fromTree((TermTree) ((TermNode)o).getGraph()));
                }

                o = elements[9];
                TermNodeDto ratioTo = null;
                if (o instanceof TermNode){
                    ratioTo = TermNodeDto.fromNode((TermNode) o, TermTreeDto.fromTree((TermTree) ((TermNode)o).getGraph()));
                }


                TermDto termDto = new CharacterDto(
                        uuid,
                        representations,
                        (UUID)elements[2],
                        (UUID)elements[3],
                        (UUID)elements[4],
                        null,
                        (String)elements[5],
//                        vocRepresentations,
                        isAvailableForTaxon,
                        isAvailableForTaxonName,
                        isAvailableForOccurrence,
                        (String)elements[10],structure, null, prop, null, ratioTo, isSupportsCategoricalData,
//                        structure, structureModifier, prop, null, isSupportsCategoricalData,
                        isSupportsQuantitativeData,
                        supportedCategoricalDtos,
                        recommendedModifierDtos,
                        recommendedMeasurementUnitsDtos,
                        recommendedStatisticalMeasuresDtos)
                        ;
                termDto.setUri((URI)elements[7]);
                termDto.setMedia(mediaUuids);


                dtoMap.put(uuid, termDto);
                dtos.add(termDto);
            }
        }
        return dtos;
    }



}

