package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;


public class FieldUnitDTO extends DerivateDTO{

	//Row Attributes
	private String country;
	private String collection;
	private String date;
	private String herbarium;
	private boolean hasType;

	private List<PreservedSpecimenDTO> preservedSpecimenDTOs;
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
     * @return the collection
     */
    public String getCollection() {
        return collection;
    }
    /**
     * @param collection the collection to set
     */
    public void setCollection(String collection) {
        this.collection = collection;
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
     * @return the herbarium
     */
    public String getHerbarium() {
        return herbarium;
    }
    /**
     * @param herbarium the herbarium to set
     */
    public void setHerbarium(String herbarium) {
        this.herbarium = herbarium;
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

    /**
     * @return the derivateDTOs
     */
    public List<PreservedSpecimenDTO> getPreservedSpecimenDTOs() {
        return preservedSpecimenDTOs;
    }

    public void addPreservedSpecimenDTO(PreservedSpecimenDTO preservedSpecimenDTO){
        if(preservedSpecimenDTOs==null){
            preservedSpecimenDTOs = new ArrayList<PreservedSpecimenDTO>();
        }
        preservedSpecimenDTOs.add(preservedSpecimenDTO);
    }

    public void setPreservedSpecimenDTOs(List<PreservedSpecimenDTO> preservedSpecimenDTOs) {
        this.preservedSpecimenDTOs = preservedSpecimenDTOs;
    }

}
