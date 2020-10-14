/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.model.occurrence.FieldUnit;

public class FieldUnitDTO extends SpecimenOrObservationBaseDTO {

    private static final long serialVersionUID = 3981843956067273220L;

    //Row Attributes
	private String country;
	private String collectingString;
	private String date;
	private String collectionsStatistics;

	private boolean hasType;

	private GatheringEventDTO gatheringEvent;

	public static FieldUnitDTO fromEntity(FieldUnit entity){
        if(entity == null) {
            return null;
        }
        return new FieldUnitDTO(entity);
	}

    private FieldUnitDTO(FieldUnit fieldUnit) {
        super(fieldUnit);
        if (fieldUnit.getGatheringEvent() != null){
            gatheringEvent = GatheringEventDTO.newInstance(fieldUnit.getGatheringEvent());
        }
        setRecordBase(fieldUnit.getRecordBasis().getMessage());
        setListLabel(fieldUnit.getTitleCache());
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

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public boolean isHasType() {
        return hasType;
    }
    public void setHasType(boolean hasType) {
        this.hasType = hasType;
    }

    public GatheringEventDTO getGatheringEvent() {
        return gatheringEvent;
    }
    public void setGatheringEvent(GatheringEventDTO gatheringEvent) {
        this.gatheringEvent = gatheringEvent;
    }
}