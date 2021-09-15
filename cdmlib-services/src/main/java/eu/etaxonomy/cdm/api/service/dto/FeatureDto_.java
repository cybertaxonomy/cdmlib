/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmClass;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;

/**
 * @author k.luther
 * @since Aug 25, 2021
 */
public class FeatureDto_ extends TermDto {

    private static final long serialVersionUID = -5138575401281727741L;


    public FeatureDto_(UUID uuid, Set<Representation> representations, TermType termType, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, String idInVocabulary, String titleCache, EnumSet<CdmClass> supportedDataTypes,
            Set<TermVocabularyDto> recommendedModifierEnumeration, Set<TermDto> recommendedStatisticalMeasures,
            Set<TermVocabularyDto> supportedCategoricalEnumerations, Set<TermDto> recommendedMeasurementUnits) {
        super(uuid, representations, termType, partOfUuid, kindOfUuid, vocabularyUuid, null, idInVocabulary, titleCache);
        this.recommendedMeasurementUnits = recommendedMeasurementUnits;
        this.recommendedModifierEnumeration = recommendedModifierEnumeration;
        this.recommendedStatisticalMeasures = recommendedStatisticalMeasures;
        this.supportedCategoricalEnumerations = supportedCategoricalEnumerations;
        this.supportedDataTypes = supportedDataTypes;
    }

    public static FeatureDto_ fromFeature(Feature feature){
        FeatureDto_ dto = (FeatureDto_) fromTerm(feature);
        if (feature.isSupportsCategoricalData()){
            dto.supportedDataTypes.add(CdmClass.CATEGORICAL_DATA);
        }
        if (feature.isSupportsCommonTaxonName()){
            dto.supportedDataTypes.add(CdmClass.COMMON_TAXON_NAME);
        }
        if (feature.isSupportsDistribution()){
            dto.supportedDataTypes.add(CdmClass.DISTRIBUTION);
        }
        if (feature.isSupportsIndividualAssociation()){
            dto.supportedDataTypes.add(CdmClass.INDIVIDUALS_ASSOCIATION);
        }
        if (feature.isSupportsQuantitativeData()){
            dto.supportedDataTypes.add(CdmClass.QUANTITATIVE_DATA);
        }
        if (feature.isSupportsTaxonInteraction()){
            dto.supportedDataTypes.add(CdmClass.TAXON_INTERACTION);
        }
        if (feature.isSupportsTemporalData()){
            dto.supportedDataTypes.add(CdmClass.TEMPORAL_DATA);
        }
        if (!feature.isSupportsTextData()){
            dto.supportedDataTypes.remove(CdmClass.TEXT_DATA);
        }
        TermVocabularyDto vocDto;
        for (TermVocabulary voc: feature.getRecommendedModifierEnumeration()){
            vocDto = TermVocabularyDto.fromVocabulary(voc);
            dto.recommendedModifierEnumeration.add(vocDto);
        }
        TermDto termDto;
        for (StatisticalMeasure term: feature.getRecommendedStatisticalMeasures()){
            termDto = TermDto.fromTerm(term);
            dto.recommendedStatisticalMeasures.add(termDto);
        }
        for (TermVocabulary voc: feature.getSupportedCategoricalEnumerations()){
            vocDto = TermVocabularyDto.fromVocabulary(voc);
            dto.supportedCategoricalEnumerations.add(vocDto);
        }

        for (MeasurementUnit term: feature.getRecommendedMeasurementUnits()){
            termDto = TermDto.fromTerm(term);
            dto.recommendedMeasurementUnits.add(termDto);
        }

        return dto;

    }

    private EnumSet<CdmClass> supportedDataTypes = EnumSet.of(CdmClass.TEXT_DATA);

    private Set<TermVocabularyDto> recommendedModifierEnumeration = new HashSet<>();

    private Set<TermDto> recommendedStatisticalMeasures = new HashSet<>();

    private Set<TermVocabularyDto> supportedCategoricalEnumerations = new HashSet<>();

    private Set<TermDto> recommendedMeasurementUnits = new HashSet<>();


    public EnumSet<CdmClass> getSupportedDataTypes() {
        return supportedDataTypes;
    }


    public void setSupportedDataTypes(EnumSet<CdmClass> supportedDataTypes) {
        this.supportedDataTypes = supportedDataTypes;
    }


    public Set<TermVocabularyDto> getRecommendedModifierEnumeration() {
        return recommendedModifierEnumeration;
    }


    public void setRecommendedModifierEnumeration(Set<TermVocabularyDto> recommendedModifierEnumeration) {
        this.recommendedModifierEnumeration = recommendedModifierEnumeration;
    }


    public Set<TermDto> getRecommendedStatisticalMeasures() {
        return recommendedStatisticalMeasures;
    }


    public void setRecommendedStatisticalMeasures(Set<TermDto> recommendedStatisticalMeasures) {
        this.recommendedStatisticalMeasures = recommendedStatisticalMeasures;
    }


    public Set<TermVocabularyDto> getSupportedCategoricalEnumerations() {
        return supportedCategoricalEnumerations;
    }


    public void setSupportedCategoricalEnumerations(Set<TermVocabularyDto> supportedCategoricalEnumerations) {
        this.supportedCategoricalEnumerations = supportedCategoricalEnumerations;
    }


    public Set<TermDto> getRecommendedMeasurementUnits() {
        return recommendedMeasurementUnits;
    }


    public void setRecommendedMeasurementUnits(Set<TermDto> recommendedMeasurementUnits) {
        this.recommendedMeasurementUnits = recommendedMeasurementUnits;
    }



}
