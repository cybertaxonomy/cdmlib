package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.occurrence.FieldUnit;



public class FieldUnitDTO extends DerivateDTO{
    //Row Attributes
	private String country;
	private String collectionString;
	private String date;

	private boolean hasType;
	private List<UUID> taxonRelatedDerivedUnits = new ArrayList<>();



 //   private List<PreservedSpecimenDTO> preservedSpecimenDTOs;
	private GatheringEventDTO gatheringEvent;


	/**
     * @param fieldUnit
     */
    public FieldUnitDTO(FieldUnit fieldUnit) {
        super(fieldUnit);
    }


    public static FieldUnitDTO newInstance(FieldUnit fieldUnit){
	    FieldUnitDTO fieldUnitDto = new FieldUnitDTO(fieldUnit);
	    fieldUnitDto.gatheringEvent = GatheringEventDTO.newInstance(fieldUnit.getGatheringEvent());
	    fieldUnitDto.setRecordBase(fieldUnit.getRecordBasis().getMessage());
	    fieldUnitDto.setListLabel(fieldUnit.getTitleCache());

	    return fieldUnitDto;

	}




//=======
//	private String protologue;
//	private String kindOfUnit;
//	private List<UUID> taxonRelatedDerivedUnits = new ArrayList<>();
//	private List<Media> listOfMedia = new ArrayList<>();
//
//
//    private List<PreservedSpecimenDTO> preservedSpecimenDTOs;
//	private GatheringEventDTO gatheringEvent;
//
//
//	public static FieldUnitDTO newInstance(FieldUnit fieldUnit){
//	    FieldUnitDTO fieldUnitDto = new FieldUnitDTO();
//	    fieldUnitDto.kindOfUnit = fieldUnit.getKindOfUnit().getTitleCache();
//	    fieldUnitDto.gatheringEvent = GatheringEventDTO.newInstance(fieldUnit.getGatheringEvent());
//	    fieldUnitDto.setUuid(fieldUnit.getUuid());
//	    fieldUnitDto.setTitleCache(fieldUnit.getTitleCache());
//
//	    Set<DescriptionBase<IIdentifiableEntityCacheStrategy<FieldUnit>>> descriptions = fieldUnit.getDescriptions();
//	    for (DescriptionBase desc : descriptions){
//	        if (desc instanceof SpecimenDescription){
//	            SpecimenDescription specimenDesc = (SpecimenDescription)desc;
//    	        if (specimenDesc.isImageGallery()){
//    	            for (DescriptionElementBase element : specimenDesc.getElements()){
//    	                if (element.isInstanceOf(TextData.class)&& element.getFeature().equals(Feature.IMAGE())) {
//	                        for (Media media :element.getMedia()){
//	                            fieldUnitDto.listOfMedia.add(media);
//	                        }
//    	                }
//    	            }
//    	        }
//	        }
//	    }
//	    return fieldUnitDto;
//
//	}
//
//
//	/**
//     * @return the listOfMedia
//     */
//    public List<Media> getListOfMedia() {
//        return listOfMedia;
//    }
//
//    /**
//     * @param listOfMedia the listOfMedia to set
//     */
//    public void setListOfMedia(List<Media> listOfMedia) {
//        this.listOfMedia = listOfMedia;
//    }
//
//>>>>>>> Stashed changes




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
    public String getCollection() {
        return collectionString;
    }
    /**
     * @param collectionString the collectionString to set
     */
    public void setCollection(String collection) {
        this.collectionString = collection;
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
