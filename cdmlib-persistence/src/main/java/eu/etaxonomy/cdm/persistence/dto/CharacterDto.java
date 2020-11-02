/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.Representation;
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


    /**
     * @param uuid
     * @param representations
     * @param partOfUuid
     * @param kindOfUuid
     * @param vocabularyUuid
     * @param orderIndex
     * @param idInVocabulary
     * @param vocRepresentations
     * @param isAvailableForTaxon
     * @param isAvailableForTaxonName
     * @param isAvailableForOccurrence
     * @param titleCache
     * @param structure
     * @param structureModifier
     * @param property
     * @param propertyModifier
     */
    public CharacterDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary, Set<Representation> vocRepresentations,
            boolean isAvailableForTaxon, boolean isAvailableForTaxonName, boolean isAvailableForOccurrence,
            String titleCache, TermNodeDto structure, TermDto structureModifier, TermNodeDto property,
            TermDto propertyModifier, boolean isSupportCategoricalData, boolean isSupportsQuantitativeData, Set<TermVocabularyDto> supportedCategoricalEnumerations, Set<TermVocabularyDto> recommendedModifierEnumeration,  Set<TermDto> recommendedMeasurementUnits,  Set<TermDto> recommendedStatisticalMeasures) {
        super(uuid, representations, partOfUuid, kindOfUuid, vocabularyUuid, orderIndex, idInVocabulary,
                vocRepresentations, isAvailableForTaxon, isAvailableForTaxonName, isAvailableForOccurrence, titleCache, isSupportCategoricalData, isSupportsQuantitativeData, supportedCategoricalEnumerations, recommendedModifierEnumeration, recommendedMeasurementUnits, recommendedStatisticalMeasures);
        this.structure = structure;
        this.structureModifier = structureModifier;
        this.property = property;
        this.propertyModifier = propertyModifier;
        this.setTermType(TermType.Character);
    }

    /**
     * @param character
     */
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
       CharacterDto dto = new CharacterDto(character.getUuid(), character.getRepresentations(), character.getPartOf() != null? character.getPartOf().getUuid(): null, character.getKindOf() != null? character.getKindOf().getUuid(): null, voc != null? voc.getUuid(): null,
               null, character.getIdInVocabulary(), voc != null? voc.getRepresentations(): null, character.isAvailableForTaxon(), character.isAvailableForTaxonName(), character.isAvailableForOccurrence(), character.getTitleCache(),
                       character.getStructure() !=null? TermNodeDto.fromNode(character.getStructure()): null, character.getStructureModifier() != null? TermDto.fromTerm(character.getStructureModifier()): null, character.getProperty() != null? TermNodeDto.fromNode(character.getProperty()): null,
                        character.getPropertyModifier() != null? TermDto.fromTerm(character.getPropertyModifier()): null, character.isSupportsCategoricalData(), character.isSupportsQuantitativeData(),supportedCategoricalDtos, recommendedModifierDtos, recommendedMeasurementUnitsDtos, recommendedStatisticalMeasuresDtos);

       return dto;
    }



    public TermNodeDto getStructure() {
        return structure;
    }

    public void setStructure(TermNodeDto structure) {
        this.structure = structure;
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


}
