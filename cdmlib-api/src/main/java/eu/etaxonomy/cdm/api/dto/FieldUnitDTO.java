/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.Partial;

import eu.etaxonomy.cdm.model.occurrence.FieldUnit;

public class FieldUnitDTO extends SpecimenOrObservationBaseDTO<FieldUnit> {

    private static final long serialVersionUID = 3981843956067273220L;

	private String country;
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

    //******************** CONSTRUCTOR ***********************************/

    //TODO remove model dependency
    public FieldUnitDTO(Class<FieldUnit> type, UUID uuid, String label) {
        super(type, uuid, label);
    }

    //****************** GETTER / SETTER ******************************************/

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
        boolean hasType = getDerivatives()
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

    public CollectionDTO getCollection() {
        return collection;
    }

    public void setCollectioDTO(CollectionDTO collection) {
        this.collection = collection;
    }

    @Override
    public void updateTreeDependantData(Set<DerivedUnitDTO> derivatives) {
        TreeLabels treeLabels = assembleLablesFromTree(true, true);
        setSummaryLabel(treeLabels.summaryLabel);
        setCollectionStatistics(treeLabels.collectionsStatistics);
        super.updateTreeDependantData(derivatives);
    }

    /**
     * Walks the tree of sub-derivatives to collect the summary label and the collection statistics.
     * The latter lists all collections with the number of specimens which are involved in this tree.
     */
    private TreeLabels assembleLablesFromTree(
            boolean doSummaryLabel, boolean doCollectionsStatistics) {

        final String SEPARATOR_STRING = ", ";

        TreeLabels treeLabels = new TreeLabels();
        Map<CollectionDTO, List<String> > unitIdenfierLabelsByCollections = new HashMap<>();

        // TODO collectDerivatives(maxDepth)
        for(DerivedUnitDTO subDTO : collectDerivatives(this)) {
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
            String summaryLabel = getLabel();
            List<String> derivativesAccessionNumbers = new ArrayList<>();
            for(List<String> labels : unitIdenfierLabelsByCollections.values()) {
                derivativesAccessionNumbers.addAll(labels);
            }
            derivativesAccessionNumbers = derivativesAccessionNumbers.stream().filter(s -> s != null).sorted().collect(Collectors.toList());
            if (!derivativesAccessionNumbers.isEmpty()) {
                summaryLabel += " (" + String.join(SEPARATOR_STRING, derivativesAccessionNumbers) +  ")";
            }
            treeLabels.summaryLabel = summaryLabel;
        }

        if(doCollectionsStatistics) {
            List<String> collectionStats = new ArrayList<>();
            for (CollectionDTO collectionDTO : unitIdenfierLabelsByCollections.keySet()) {
                int unitCount = unitIdenfierLabelsByCollections.get(collectionDTO).size();
                if (collectionDTO.getCode() != null) {
                    collectionStats.add(collectionDTO.getCode() + (unitCount > 1 ? "(" + unitCount + ")" : ""));
                }
            }
            Collections.sort(collectionStats);
            treeLabels.collectionsStatistics = String.join(SEPARATOR_STRING, collectionStats);
        }

        return treeLabels;
    }

    /**
     * Recursively collects all derivatives from this.
     */
    public static Collection<DerivedUnitDTO> collectDerivatives(SpecimenOrObservationBaseDTO<?> dto) {
        return collectDerivatives(dto, new HashSet<>());
    }

    /**
     * Private partner method to {@link #collectDerivatives()} for recursive calls.
     */
    private static Collection<DerivedUnitDTO> collectDerivatives(SpecimenOrObservationBaseDTO<?> dto,
            Set<DerivedUnitDTO> dtos) {

        Set<DerivedUnitDTO> derivatives = dto.getDerivatives();
        dtos.addAll(derivatives);
        for(DerivedUnitDTO subDto : derivatives) {
            dtos.addAll(collectDerivatives(subDto, dtos));
        }
        return dtos;
    }

    static class TreeLabels {
        String summaryLabel = null;
        String collectionsStatistics = null;
    }
}