package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.occurrence.FieldUnit;



public class FieldUnitDTO extends DerivateDTO{

    private static final long serialVersionUID = 3981843956067273220L;

    //Row Attributes
	private String country;
	private String collectingString;
	private String date;
	private String collectionString;

	private boolean hasType;
	private List<UUID> taxonRelatedDerivedUnits = new ArrayList<>();

	private GatheringEventDTO gatheringEvent;


	/**
     * @param fieldUnit
     */
    public FieldUnitDTO(FieldUnit fieldUnit) {
        super(fieldUnit);
    }


    public static FieldUnitDTO newInstance(FieldUnit fieldUnit){
	    FieldUnitDTO fieldUnitDto = new FieldUnitDTO(fieldUnit);
	    if (fieldUnit.getGatheringEvent() != null){
	        fieldUnitDto.gatheringEvent = GatheringEventDTO.newInstance(fieldUnit.getGatheringEvent());
	    }
	    fieldUnitDto.setRecordBase(fieldUnit.getRecordBasis().getMessage());
	    fieldUnitDto.setListLabel(fieldUnit.getTitleCache());

	    return fieldUnitDto;

	}

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }
    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }
    /**
     * @return the collectionString
     */
    @Override
    public String getCollection() {
        return collectionString;
    }
    /**
     * @param collectionString the collectionString to set
     */
    @Override
    public void setCollection(String collection) {
        this.collectionString = collection;
    }

    /**
     * @return the collectionString
     */

    public String getCollectingString() {
        return collectingString;
    }
    /**
     * @param collectionString the collectionString to set
     */
    public void setCollectingString(String collectingString) {
        this.collectingString = collectingString;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }


    /**
     * @return the hasType
     */
    public boolean isHasType() {
        return hasType;
    }
    /**
     * @param hasType the hasType to set
     */
    public void setHasType(boolean hasType) {
        this.hasType = hasType;
    }

    public GatheringEventDTO getGatheringEvent() {
        return gatheringEvent;
    }
    public void setGatheringEvent(GatheringEventDTO gatheringEvent) {
        this.gatheringEvent = gatheringEvent;
    }


    /**
     * @return the taxonRelatedDerivedUnits
     */
    public List<UUID> getTaxonRelatedDerivedUnits() {
        return taxonRelatedDerivedUnits;
    }


    /**
     * @param taxonRelatedDerivedUnits the taxonRelatedDerivedUnits to set
     */
    public void setTaxonRelatedDerivedUnits(List<UUID> taxonRelatedDerivedUnits) {
        this.taxonRelatedDerivedUnits = taxonRelatedDerivedUnits;
    }


    /**
     * @param derivedUnitDTO
     */
    public void addTaxonRelatedDerivedUnits(DerivateDTO derivedUnitDTO) {
        if (this.taxonRelatedDerivedUnits == null){
            this.taxonRelatedDerivedUnits = new ArrayList<>();
        }
        this.taxonRelatedDerivedUnits.add(derivedUnitDTO.getUuid());

    }

}
