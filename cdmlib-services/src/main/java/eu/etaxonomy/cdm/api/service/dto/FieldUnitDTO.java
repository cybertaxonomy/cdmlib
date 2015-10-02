package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class FieldUnitDTO extends DerivateDTO{

    private UUID uuid;

	//Row Attributes
    private static final long serialVersionUID = -4537819092486130385L;

    //Row Attributes
	private String country;
	private String collection;
	private String date;
	private String herbarium;
	private boolean hasType;
	private String protologue;

	private List<PreservedSpecimenDTO> preservedSpecimenDTOs;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getCollection() {
        return collection;
    }
    public void setCollection(String collection) {
        this.collection = collection;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getHerbarium() {
        return herbarium;
    }
    public void setHerbarium(String herbarium) {
        this.herbarium = herbarium;
    }
    public boolean isHasType() {
        return hasType;
    }
    public void setHasType(boolean hasType) {
        this.hasType = hasType;
    }
    public String getTaxonName() {
        return taxonName;
    }
    public void setTaxonName(String taxonName) {
        this.taxonName = taxonName;
    }
    public String getProtologue() {
        return protologue;
    }
    public void setProtologue(String protologue) {
        this.protologue = protologue;
    }

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
