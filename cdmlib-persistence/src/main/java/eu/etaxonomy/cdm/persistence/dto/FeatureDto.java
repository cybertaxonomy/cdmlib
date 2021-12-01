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
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmClass;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author k.luther
 * @since Jun 11, 2020
 */
public class FeatureDto extends TermDto {

    private static final long serialVersionUID = 5123011385890020838L;

    boolean isAvailableForTaxon = true;
    boolean isAvailableForTaxonName = true;
    boolean isAvailableForOccurrence = true;
    boolean isSupportsCategoricalData = false;
    boolean isSupportsQuantitativeData = false;

    Set<TermDto> recommendedMeasurementUnits = new HashSet<>();
    Set<TermDto> recommendedStatisticalMeasures = new HashSet<>();
    Set<TermVocabularyDto> supportedCategoricalEnumerations = new HashSet<>();
    Set<TermVocabularyDto> recommendedModifierEnumeration = new HashSet<>();

    public FeatureDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary,
            boolean isAvailableForTaxon, boolean isAvailableForTaxonName, boolean isAvailableForOccurrence, String titleCache, boolean isSupportsCategoricalData, boolean isSupportsQuantitativeData,
            Set<TermVocabularyDto> supportedCategoricalEnumerations, Set<TermVocabularyDto> recommendedModifierEnumeration,  Set<TermDto> recommendedMeasurementUnits,  Set<TermDto> recommendedStatisticalMeasures){
        super(uuid, representations, TermType.Feature, partOfUuid, kindOfUuid,
                vocabularyUuid, orderIndex, idInVocabulary, titleCache);
        this.isAvailableForOccurrence = isAvailableForOccurrence;
        this.isAvailableForTaxon = isAvailableForTaxon;
        this.isAvailableForTaxonName = isAvailableForTaxonName;

        this.isSupportsCategoricalData = isSupportsCategoricalData;
        this.isSupportsQuantitativeData = isSupportsQuantitativeData;

        this.recommendedMeasurementUnits = recommendedMeasurementUnits;
        this.recommendedStatisticalMeasures = recommendedStatisticalMeasures;
        this.supportedCategoricalEnumerations = supportedCategoricalEnumerations;
        this.recommendedModifierEnumeration = recommendedModifierEnumeration;
    }



    static public FeatureDto fromFeature(Feature term) {
        UUID partOfUuid = term.getPartOf() != null? term.getPartOf().getUuid(): null;
        UUID kindOfUuid = term.getKindOf() != null? term.getKindOf().getUuid(): null;
        TermVocabulary<?> vocabulary = HibernateProxyHelper.deproxy(term.getVocabulary());
        UUID vocUuid =  vocabulary != null? vocabulary.getUuid(): null;
        Set<TermVocabularyDto> supportedCategoricalEnumerations = new HashSet<>();
        for (TermVocabulary<State> stateVoc:term.getSupportedCategoricalEnumerations()){
            supportedCategoricalEnumerations.add(TermVocabularyDto.fromVocabulary(stateVoc));
        }

        Set<TermVocabularyDto> recommendedModifiers = new HashSet<>();
        for (TermVocabulary<DefinedTerm> modifier:term.getRecommendedModifierEnumeration()){
            recommendedModifiers.add(TermVocabularyDto.fromVocabulary(modifier));
        }

        Set<TermDto> recommendedMeasurementUnits= new HashSet<>();
        for (MeasurementUnit measurementUnit:term.getRecommendedMeasurementUnits()){
            recommendedMeasurementUnits.add(TermDto.fromTerm(measurementUnit));
        }

        Set<TermDto> recommendedStatisticalMeasures = new HashSet<>();
        for (StatisticalMeasure statMeasure:term.getRecommendedStatisticalMeasures()){
            recommendedStatisticalMeasures.add(TermDto.fromTerm(statMeasure));
        }

        FeatureDto result =  new FeatureDto(term.getUuid(), term.getRepresentations(), partOfUuid, kindOfUuid, vocUuid, null, term.getIdInVocabulary(), term.isAvailableForTaxon(), term.isAvailableForTaxonName(), term.isAvailableForOccurrence(),
                term.getTitleCache(),term.isSupportsCategoricalData(), term.isSupportsQuantitativeData(), supportedCategoricalEnumerations, recommendedModifiers, recommendedMeasurementUnits, recommendedStatisticalMeasures);
        result.isAvailableForOccurrence = term.isAvailableForOccurrence();
        result.isAvailableForTaxon = term.isAvailableForTaxon();
        result.isAvailableForTaxonName = term.isAvailableForTaxonName();
        result.isSupportsCategoricalData = term.isSupportsCategoricalData();
        result.isSupportsQuantitativeData = term.isSupportsQuantitativeData();
        if (term.getRecommendedMeasurementUnits() != null && !term.getRecommendedMeasurementUnits().isEmpty()){
            result.recommendedMeasurementUnits = new HashSet<>();
        }
        for (MeasurementUnit unit: term.getRecommendedMeasurementUnits()){
            result.recommendedMeasurementUnits.add(TermDto.fromTerm(unit));
        }

        if (term.getRecommendedStatisticalMeasures() != null && !term.getRecommendedStatisticalMeasures().isEmpty()){
            result.recommendedStatisticalMeasures = new HashSet<>();
        }
        for (StatisticalMeasure unit: term.getRecommendedStatisticalMeasures()){
            result.recommendedStatisticalMeasures.add(TermDto.fromTerm(unit));
        }

        if (term.getSupportedCategoricalEnumerations() != null && !term.getSupportedCategoricalEnumerations().isEmpty()){
            result.supportedCategoricalEnumerations = new HashSet<>();
        }
        for (TermVocabulary<State> voc: term.getSupportedCategoricalEnumerations()){
            result.supportedCategoricalEnumerations.add(new TermVocabularyDto(voc.getUuid(), voc.getRepresentations(), voc.getTermType(), voc.getTitleCache(), voc.isAllowDuplicates(), voc.isOrderRelevant(), voc.isFlat()));
        }

        if (term.getRecommendedModifierEnumeration() != null && !term.getRecommendedModifierEnumeration().isEmpty()){
            result.recommendedModifierEnumeration = new HashSet<>();
        }
        for (TermVocabulary<DefinedTerm> voc: term.getRecommendedModifierEnumeration()){
            result.recommendedModifierEnumeration.add(new TermVocabularyDto(voc.getUuid(), voc.getRepresentations(), voc.getTermType(), voc.getTitleCache(), voc.isAllowDuplicates(), voc.isOrderRelevant(), voc.isFlat()));
        }
        return result;
    }

    /**
     * @return the isAvailableForTaxon
     */
    public boolean isAvailableForTaxon() {
        return isAvailableForTaxon;
    }

    /**
     * @param isAvailableForTaxon the isAvailableForTaxon to set
     */
    public void setAvailableForTaxon(boolean isAvailableForTaxon) {
        this.isAvailableForTaxon = isAvailableForTaxon;
    }

    /**
     * @return the isAvailableForTaxonName
     */
    public boolean isAvailableForTaxonName() {
        return isAvailableForTaxonName;
    }

    /**
     * @param isAvailableForTaxonName the isAvailableForTaxonName to set
     */
    public void setAvailableForTaxonName(boolean isAvailableForTaxonName) {
        this.isAvailableForTaxonName = isAvailableForTaxonName;
    }

    /**
     * @return the isAvailableForOccurrence
     */
    public boolean isAvailableForOccurrence() {
        return isAvailableForOccurrence;
    }

    /**
     * @param isAvailableForOccurrence the isAvailableForOccurrence to set
     */
    public void setAvailableForOccurrence(boolean isAvailableForOccurrence) {
        this.isAvailableForOccurrence = isAvailableForOccurrence;
    }

    /**
     * @return the isSupportsCategoricalData
     */
    public boolean isSupportsCategoricalData() {
        return isSupportsCategoricalData;
    }

    /**
     * @param isSupportsCategoricalData the isSupportsCategoricalData to set
     */
    public void setSupportsCategoricalData(boolean isSupportsCategoricalData) {
        this.isSupportsCategoricalData = isSupportsCategoricalData;
    }

    /**
     * @return the isSupportsQuantitativeData
     */
    public boolean isSupportsQuantitativeData() {
        return isSupportsQuantitativeData;
    }

    /**
     * @param isSupportsQuantitativeData the isSupportsQuantitativeData to set
     */
    public void setSupportsQuantitativeData(boolean isSupportsQuantitativeData) {
        this.isSupportsQuantitativeData = isSupportsQuantitativeData;
    }

    /**
     * @return the recommendedMeasurementUnits
     */
    public Set<TermDto> getRecommendedMeasurementUnits() {
        return recommendedMeasurementUnits;
    }

    /**
     * @param recommendedMeasurementUnits the recommendedMeasurementUnits to set
     */
    public void setRecommendedMeasurementUnits(Set<TermDto> recommendedMeasurementUnits) {
        this.recommendedMeasurementUnits = recommendedMeasurementUnits;
    }

    /**
     * @return the recommendedStatisticalMeasures
     */
    public Set<TermDto> getRecommendedStatisticalMeasures() {
        return recommendedStatisticalMeasures;
    }

    /**
     * @param recommendedStatisticalMeasures the recommendedStatisticalMeasures to set
     */
    public void setRecommendedStatisticalMeasures(Set<TermDto> recommendedStatisticalMeasures) {
        this.recommendedStatisticalMeasures = recommendedStatisticalMeasures;
    }

    /**
     * @return the supportedCategoricalEnumerations
     */
    public Set<TermVocabularyDto> getSupportedCategoricalEnumerations() {
        return supportedCategoricalEnumerations;
    }

    /**
     * @param supportedCategoricalEnumerations the supportedCategoricalEnumerations to set
     */
    public void setSupportedCategoricalEnumerations(Set<TermVocabularyDto> supportedCategoricalEnumerations) {
        this.supportedCategoricalEnumerations = supportedCategoricalEnumerations;
    }

    /**
     * @return the recommendedModifierEnumeration
     */
    public Set<TermVocabularyDto> getRecommendedModifierEnumeration() {
        return recommendedModifierEnumeration;
    }

    /**
     * @param recommendedModifierEnumeration the recommendedModifierEnumeration to set
     */
    public void setRecommendedModifierEnumeration(Set<TermVocabularyDto> recommendedModifierEnumeration) {
        this.recommendedModifierEnumeration = recommendedModifierEnumeration;
    }

    public static String getTermDtoSelect(){
        String[] result = createSqlParts("DefinedTermBase");

        return result[0]+result[1]+result[2];
    }

    private static String[] createSqlParts(String fromTable) {
        String sqlSelectString = ""
                + "select a.uuid, "
                + "r, "
                + "p.uuid, "
                + "k.uuid, "
                + "v.uuid, "
                + "a.orderIndex, "
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
                + "recommendedMeasurementUnits ";

        String sqlFromString =   " from "+fromTable+" as a ";

        String sqlJoinString =  "LEFT JOIN a.partOf as p "
                + "LEFT JOIN a.kindOf as k "
                + "LEFT JOIN a.media as m "
                + "LEFT JOIN a.representations AS r "
                + "LEFT JOIN a.vocabulary as v "
//                + "LEFT JOIN v.representations as voc_rep "
                + "LEFT JOIN a.recommendedModifierEnumeration as recommendedModifierEnumeration "
                + "LEFT JOIN a.recommendedStatisticalMeasures as recommendedStatisticalMeasures "
                + "LEFT JOIN a.supportedCategoricalEnumerations as supportedCategoricalEnumerations "
                + "LEFT JOIN a.recommendedMeasurementUnits as recommendedMeasurementUnits "
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
//                if(elements[7]!=null){
//                    dtoMap.get(uuid).addVocRepresentation((Representation)elements[7]);
//                }
                if(elements[9]!=null){
                    dtoMap.get(uuid).addMedia(((Media) elements[9]).getUuid());
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
                if(elements[9] instanceof Media) {
                    mediaUuids.add(((Media) elements[9]).getUuid());
                }
                // voc representation
//                Set<Representation> vocRepresentations = new HashSet<>();
//                if(elements[7] instanceof Representation) {
//                    vocRepresentations = new HashSet<Representation>(7);
//                    vocRepresentations.add((Representation)elements[7]);
//                }
                boolean isAvailableForTaxon = false;
                boolean isAvailableForTaxonName = false;
                boolean isAvailableForOccurrence = false;

                EnumSet<CdmClass> availableForString = (EnumSet<CdmClass>)elements[10];

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

                EnumSet<CdmClass> supportsString = (EnumSet<CdmClass>)elements[12];

                if (supportsString.contains(CdmClass.CATEGORICAL_DATA)){
                    isSupportsCategoricalData = true;
                }
                if (supportsString.contains(CdmClass.QUANTITATIVE_DATA)){
                    isSupportsQuantitativeData = true;
                }

                Object o = elements[13];
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



                o = elements[14];
                Set<TermDto> recommendedStatisticalMeasuresDtos = new HashSet<>();
                if (o instanceof StatisticalMeasure){
                    recommendedStatisticalMeasuresDtos.add(TermDto.fromTerm((StatisticalMeasure)o));
                }else if (o instanceof Set){
                    Set<StatisticalMeasure> recommendedStatisticalMeasures = new HashSet((Set<StatisticalMeasure>) o);
                    if (recommendedStatisticalMeasures != null) {
                        for (StatisticalMeasure term: recommendedStatisticalMeasures){
                            recommendedStatisticalMeasuresDtos.add(TermDto.fromTerm(term));
                        }
                    }
                }
                o =  elements[15];
                Set<TermVocabularyDto> supportedCategoricalDtos = new HashSet<>();
                if (o instanceof TermVocabulary){
                    supportedCategoricalDtos.add(TermVocabularyDto.fromVocabulary((TermVocabulary)o));
                }else if (o instanceof Set){
                    Set<TermVocabulary> supportedCategoricalEnumerations = (Set<TermVocabulary>)o;
                    for (TermVocabulary<State> voc: supportedCategoricalEnumerations){
                        supportedCategoricalDtos.add(TermVocabularyDto.fromVocabulary(voc));
                    }
                }

//                if (supportedCategoricalEnumerations != null){
//                    for (TermVocabulary<State> voc: supportedCategoricalEnumerations){
//                        supportedCategoricalDtos.add(TermVocabularyDto.fromVocabulary(voc));
//                    }
//                }
                o = elements[16];
                Set<TermDto> recommendedMeasurementUnitsDtos = new HashSet<>();
                if (o instanceof MeasurementUnit){
                    recommendedMeasurementUnitsDtos.add(TermDto.fromTerm((MeasurementUnit)o));
                }else if (o instanceof Set){
                    Set<MeasurementUnit> recommendedMeasurementUnits = (Set<MeasurementUnit>) elements[16];
                    for (MeasurementUnit term: recommendedMeasurementUnits){
                        recommendedMeasurementUnitsDtos.add(TermDto.fromTerm(term));
                    }
                }

//                if (recommendedMeasurementUnits != null){
//                    for (MeasurementUnit term: recommendedMeasurementUnits){
//                        recommendedMeasurementUnitsDtos.add(TermDto.fromTerm(term));
//                    }
//                }

                TermDto termDto = new FeatureDto(
                        uuid,
                        representations,
                        (UUID)elements[2],
                        (UUID)elements[3],
                        (UUID)elements[4],
                        (Integer)elements[5],
                        (String)elements[6],
//                        vocRepresentations,
                        isAvailableForTaxon,
                        isAvailableForTaxonName,
                        isAvailableForOccurrence,
                        (String)elements[11],
                        isSupportsCategoricalData,
                        isSupportsQuantitativeData,
                        supportedCategoricalDtos,
                        recommendedModifierDtos,
                        recommendedMeasurementUnitsDtos,
                        recommendedStatisticalMeasuresDtos)
                        ;
                termDto.setUri((URI)elements[8]);
                termDto.setMedia(mediaUuids);


                dtoMap.put(uuid, termDto);
                dtos.add(termDto);
            }
        }
        return dtos;
    }


}
