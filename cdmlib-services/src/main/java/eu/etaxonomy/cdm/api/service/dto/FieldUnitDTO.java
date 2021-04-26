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

import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

public class FieldUnitDTO extends SpecimenOrObservationBaseDTO {

    private static final long serialVersionUID = 3981843956067273220L;

    private static final String SEPARATOR_STRING = ", ";

	private String country; // TODO remove this obsolete copy of gatheringEvent.timeperiod
	private Partial date; // TODO remove this obsolete copy of gatheringEvent.timeperiod
	private String collectingString;
	private String collectionsStatistics;
	private String fieldNumber;
	private String fieldNotes;


	private GatheringEventDTO gatheringEvent;

    private CollectionDTO collection;

    private String catalogNumber;

    private String barcode;

    private String preservationMethod;

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
     * @param maxDepth
     *     The max number of levels to walk into the derivation tree, <code>null</code> means unlimited.
     * @param typeIncludeFilter
     *     Set of SpecimenOrObservationType to be included into the collection of {@link #getDerivatives() derivative DTOs}
     */
    public static FieldUnitDTO fromEntity(FieldUnit entity, Integer maxDepth, EnumSet<SpecimenOrObservationType> typeIncludeFilter){
        if(entity == null) {
            return null;
        }
        return new FieldUnitDTO(entity, maxDepth, typeIncludeFilter);
    }

	/**
     * Factory method for the construction of a FieldUnitDTO.
     * <p>
     * The direct derivatives are added to the field {@link #getDerivatives() derivates}.
     *
     *
     * @param fieldUnit
     *     The FieldUnit entity to create a DTO for. Is null save.
     * @param typeIncludeFilter
     *     Set of SpecimenOrObservationType to be included into the collection of {@link #getDerivatives() derivative DTOs}
     * @deprecated use {@link #fromEntity(FieldUnit, Integer, EnumSet)}
     */
    @Deprecated
	public static FieldUnitDTO fromEntity(FieldUnit entity, EnumSet<SpecimenOrObservationType> typeIncludeFilter){
        return fromEntity(entity, null, typeIncludeFilter);
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
    private FieldUnitDTO(FieldUnit fieldUnit, Integer maxDepth, EnumSet<SpecimenOrObservationType> typeIncludeFilter ) {
        super(fieldUnit);

        setFieldNotes(fieldUnit.getFieldNotes());
        setFieldNumber(fieldUnit.getFieldNumber());
        if(typeIncludeFilter == null) {
            typeIncludeFilter = EnumSet.allOf(SpecimenOrObservationType.class);
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
            AgentBase<?> collector = gatheringEvent.getCollector();
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


        // assemble derivate data DTO
        DerivationTreeSummaryDTO derivateDataDTO = DerivationTreeSummaryDTO.fromEntity(fieldUnit, null);
        setDerivationTreeSummary(derivateDataDTO);

        // assemble citation
        String summaryLabel = fieldUnit.getTitleCache();
        if((CdmUtils.isBlank(summaryLabel) || summaryLabel.equals(IdentifiableEntityDefaultCacheStrategy.TITLE_CACHE_GENERATION_NOT_IMPLEMENTED))
                && !fieldUnit.isProtectedTitleCache()){
            fieldUnit.setTitleCache(null);
            summaryLabel = fieldUnit.getTitleCache();
        }

        addAllDerivatives(assembleDerivatives(fieldUnit, maxDepth, typeIncludeFilter));
    }

    @Override
    public void updateTreeDependantData() {
        TreeLabels treeLabels = assembleLablesFromTree(true, true);
        setSummaryLabel(treeLabels.summaryLabel);
        setCollectionStatistics(treeLabels.collectionsStatistics);
    }

    /**
     * Walks the tree of sub-derivatives to collect the summary label and the collection statistics.
     * The latter lists all collections with the number of specimens which are involved in this tree.
     */
    public TreeLabels assembleLablesFromTree(boolean doSummaryLabel, boolean collectionsStatistics) {

        TreeLabels treeLabels = new TreeLabels();
        Map<CollectionDTO, List<String> > unitIdenfierLabelsByCollections = new HashMap<>();

        // TODO collectDerivatives(maxDepth)
        for(DerivedUnitDTO subDTO : collectDerivatives()) {
            CollectionDTO collectionDTO = subDTO.getCollection();
            if (collectionDTO != null) {
                //combine collection with identifier
                String identifier = subDTO.getMostSignificantIdentifier();
                if (identifier != null && collectionDTO.getCode()!=null) {
                    identifier = (collectionDTO.getCode()!=null?collectionDTO.getCode():"[no collection]")+" "+identifier;
                }
                if(!unitIdenfierLabelsByCollections.containsKey(collectionDTO)) {
                    unitIdenfierLabelsByCollections.put(collectionDTO, new ArrayList<>());
                }
                unitIdenfierLabelsByCollections.get(collectionDTO).add(identifier);
            }
        }

        if(doSummaryLabel) {
            String summaryLabel = "";
            List<String> derivativesAccessionNumbers = new ArrayList<>();
            for(List<String> labels : unitIdenfierLabelsByCollections.values()) {
                derivativesAccessionNumbers.addAll(labels);
            }
            if (!derivativesAccessionNumbers.isEmpty()) {
                summaryLabel += " (";
                for (String accessionNumber : derivativesAccessionNumbers) {
                    if (accessionNumber != null && !accessionNumber.isEmpty()) {
                        summaryLabel += accessionNumber + SEPARATOR_STRING;
                    }
                }
                summaryLabel = removeTail(summaryLabel, SEPARATOR_STRING);
                summaryLabel += ")";
            }
            treeLabels.summaryLabel = summaryLabel;
        }

        if(collectionsStatistics) {
            String collectionsString = "";
            for (CollectionDTO collectionDTO : unitIdenfierLabelsByCollections.keySet()) {
                int unitCount = unitIdenfierLabelsByCollections.get(collectionDTO).size();
                if (collectionDTO.getCode() != null) {
                    collectionsString += collectionDTO.getCode();
                }
                if (unitCount > 1) {
                    collectionsString += "(" + unitCount + ")";
                }
                collectionsString += SEPARATOR_STRING;
            }
            collectionsString = removeTail(collectionsString, SEPARATOR_STRING);
            treeLabels.collectionsStatistics = collectionsString;
        }

        return treeLabels;
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

    public String getFieldNumber() {
        return fieldNumber;
    }

    public void setFieldNumber(String fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    public String getFieldNotes() {
        return fieldNotes;
    }

    public void setFieldNotes(String fieldNotes) {
        this.fieldNotes = fieldNotes;
    }

    public void setCollection(CollectionDTO collection) {
        this.collection = collection;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPreservationMethod() {
        return preservationMethod;
    }

    public void setPreservationMethod(String preservationMethod) {
        this.preservationMethod = preservationMethod;
    }

    /**
     * @return the collection
     *
     * @deprecated TODO remove as it only duplicates the information contained in the collectionDTO
     */
    @Deprecated
    public String getCollectionCode() {
        if (collection != null){
            return collection.getCode();
        } else {
            return null;
        }
    }

    /**
     * @return the collection
     */
    public CollectionDTO getCollection() {
        return collection;
    }

    /**
     * @param collection the collection to set
     */
    public void setCollectioDTO(CollectionDTO collection) {
        this.collection = collection;
    }

    static class TreeLabels {
        String summaryLabel = null;
        String collectionsStatistics = null;
    }
}