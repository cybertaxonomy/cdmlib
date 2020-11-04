/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

public class FieldUnitDTO extends SpecimenOrObservationBaseDTO {

    private static final long serialVersionUID = 3981843956067273220L;

    private static final String SEPARATOR_STRING = ", ";

	private String country;
	private String collectingString;
	private Partial date;
	private String collectionsStatistics;

	private GatheringEventDTO gatheringEvent;

	public static FieldUnitDTO fromEntity(FieldUnit entity){
        return FieldUnitDTO.fromEntity(entity, null);
	}

	/**
     * Factory method for the construction of a FieldUnitDTO.
     * <p>
     * The direct derivatives are added to the field {@link #getDerivatives() derivates}.
     *
     *
     * @param fieldUnit
     *     The FieldUnit entity to create a DTO for. Is null save.
     * @param specimenOrObservationTypeFilter
     *     Set of SpecimenOrObservationType to be included into the collection of {@link #getDerivatives() derivative DTOs}
     */
	public static FieldUnitDTO fromEntity(FieldUnit entity, EnumSet<SpecimenOrObservationType> specimenOrObservationTypeFilter){
        if(entity == null) {
            return null;
        }
        return new FieldUnitDTO(entity, specimenOrObservationTypeFilter);
    }

	/**
     * The direct derivatives are added to the field {@link #getDerivatives() derivates}.
	 *
	 * @param fieldUnit
	 *     The FieldUnit entity to create a DTO for
	 * @param specimenOrObservationTypeFilter
	 *     Set of SpecimenOrObservationType to be included into the collection of {@link #getDerivatives() derivative DTOs}
	 */
    private FieldUnitDTO(FieldUnit fieldUnit, EnumSet<SpecimenOrObservationType> specimenOrObservationTypeFilter ) {
        super(fieldUnit);

        if(specimenOrObservationTypeFilter == null) {
            specimenOrObservationTypeFilter = EnumSet.allOf(SpecimenOrObservationType.class);
        }
        if (fieldUnit.getGatheringEvent() != null){
            gatheringEvent = GatheringEventDTO.newInstance(fieldUnit.getGatheringEvent());
        }
        setRecordBase(fieldUnit.getRecordBasis());

        // --------------------------------------

        if (fieldUnit.getGatheringEvent() != null) {
            GatheringEvent gatheringEvent = fieldUnit.getGatheringEvent();
            // Country
            NamedArea country = gatheringEvent.getCountry();
            setCountry(country != null ? country.getLabel() : null);
            // Collection
            AgentBase collector = gatheringEvent.getCollector();
            String fieldNumber = fieldUnit.getFieldNumber();
            String collectionString = "";
            if (collector != null || fieldNumber != null) {
                collectionString += collector != null ? collector : "";
                if (!collectionString.isEmpty()) {
                    collectionString += " ";
                }
                collectionString += (fieldNumber != null ? fieldNumber : "");
                collectionString.trim();
            }
            setCollectingString(collectionString);
            setDate(gatheringEvent.getGatheringDate());
        }

        // Herbaria map
        Map<eu.etaxonomy.cdm.model.occurrence.Collection, Integer> collectionToCountMap = new HashMap<>();
        // List of accession numbers for citation
        List<String> preservedSpecimenAccessionNumbers = new ArrayList<>();

        // NOTE!
        // the derivation graph seems to be walked two times in here
        // 1. below in the for loop
        // 2. in the call to DerivateDataDTO.fromEntity below
        Set<DerivationEvent> derivationEvents = fieldUnit.getDerivationEvents();
        for (DerivationEvent derivationEvent : derivationEvents) {
            Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
            for (DerivedUnit derivedUnit : derivatives) {
                if(!derivedUnit.isPublish()){
                    continue;
                }
                // collect accession numbers for citation
                String identifier = derivedUnit.getMostSignificantIdentifier();
                // collect collections for herbaria column
                eu.etaxonomy.cdm.model.occurrence.Collection collection = derivedUnit.getCollection();
                if (collection != null) {
                    //combine collection with identifier
                    if (identifier != null) {
                        if(collection.getCode()!=null){
                            identifier = (collection.getCode()!=null?collection.getCode():"[no collection]")+" "+identifier;
                        }
                        preservedSpecimenAccessionNumbers.add(identifier);
                    }

                    Integer herbariumCount = collectionToCountMap.get(collection);
                    if (herbariumCount == null) {
                        herbariumCount = 0;
                    }
                    collectionToCountMap.put(collection, herbariumCount + 1);
                }
                if (specimenOrObservationTypeFilter.contains(derivedUnit.getRecordBasis())) {
                    DerivedUnitDTO derivedUnitDTO = DerivedUnitDTO.fromEntity(derivedUnit, null);
                    addDerivate(derivedUnitDTO);
                    setHasCharacterData(isHasCharacterData() || derivedUnitDTO.isHasCharacterData());
                    // NOTE! the flags setHasDetailImage, setHasDna, setHasSpecimenScan are also set in
                    // setDerivateDataDTO(), see below
                    setHasDetailImage(isHasDetailImage() || derivedUnitDTO.isHasDetailImage());
                    setHasDna(isHasDna() || derivedUnitDTO.isHasDna());
                    setHasSpecimenScan(isHasSpecimenScan() || derivedUnitDTO.isHasSpecimenScan());
                }
            }
        }

        // assemble derivate data DTO
        DerivateDataDTO derivateDataDTO = DerivateDataDTO.fromEntity(fieldUnit, null);
        setDerivateDataDTO(derivateDataDTO);

        // assemble citation
        String citation = fieldUnit.getTitleCache();
        if((CdmUtils.isBlank(citation) || citation.equals(IdentifiableEntityDefaultCacheStrategy.TITLE_CACHE_GENERATION_NOT_IMPLEMENTED))
                && !fieldUnit.isProtectedTitleCache()){
            fieldUnit.setTitleCache(null);
            citation = fieldUnit.getTitleCache();
        }
        if (!preservedSpecimenAccessionNumbers.isEmpty()) {
            citation += " (";
            for (String accessionNumber : preservedSpecimenAccessionNumbers) {
                if (!accessionNumber.isEmpty()) {
                    citation += accessionNumber + SEPARATOR_STRING;
                }
            }
            citation = removeTail(citation, SEPARATOR_STRING);
            citation += ")";
        }
        setCitation(citation);

        // assemble herbaria string
        String herbariaString = "";
        for (Entry<eu.etaxonomy.cdm.model.occurrence.Collection, Integer> e : collectionToCountMap.entrySet()) {
            eu.etaxonomy.cdm.model.occurrence.Collection collection = e.getKey();
            if (collection.getCode() != null) {
                herbariaString += collection.getCode();
            }
            if (e.getValue() > 1) {
                herbariaString += "(" + e.getValue() + ")";
            }
            herbariaString += SEPARATOR_STRING;
        }
        herbariaString = removeTail(herbariaString, SEPARATOR_STRING);
        setCollectionStatistics(herbariaString);
    }

    private String removeTail(String string, final String tail) {
        if (string.endsWith(tail)) {
            string = string.substring(0, string.length() - tail.length());
        }
        return string;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getCollectionStatistics() {
        return collectionsStatistics;
    }

    public void setCollectionStatistics(String collection) {
        this.collectionsStatistics = collection;
    }

    public String getCollectingString() {
        return collectingString;
    }
    public void setCollectingString(String collectingString) {
        this.collectingString = collectingString;
    }

    public Partial getDate() {
        return date;
    }
    public void setDate(Partial date) {
        this.date = date;
    }

    public boolean isHasType() {
        boolean hasType = collectDerivatives()
                .stream()
                .anyMatch(derivedUnitDTO -> derivedUnitDTO.getSpecimenTypeDesignations() != null && !derivedUnitDTO.getSpecimenTypeDesignations().isEmpty());
        return hasType;
    }

    public GatheringEventDTO getGatheringEvent() {
        return gatheringEvent;
    }
    public void setGatheringEvent(GatheringEventDTO gatheringEvent) {
        this.gatheringEvent = gatheringEvent;
    }
}