package eu.etaxonomy.cdm.api.service.dto;

import java.util.List;

import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;

public class DerivateHierarchyDTO {
	private DerivedUnit typeUnit;
	private List<DnaSample> dnaSamples;
	private List<DerivedUnit> preservedSpecimensWithSpecimenScan;
	private int numberOfDerivates;
	private FieldUnit fieldUnit;

	private String country;
	private String collection;
	private String date;
	private List<String> herbaria;
	private boolean isType;
	private boolean hasSpecimenScan;


	/**
     * @return the typeUnit
     */
    public DerivedUnit getTypeUnit() {
        return typeUnit;
    }
    /**
     * @param typeUnit the typeUnit to set
     */
    public void setTypeUnit(DerivedUnit typeUnit) {
        this.typeUnit = typeUnit;
    }
    /**
     * @return the dnaSamples
     */
    public List<DnaSample> getDnaSamples() {
        return dnaSamples;
    }
    /**
     * @param dnaSamples the dnaSamples to set
     */
    public void setDnaSamples(List<DnaSample> dnaSamples) {
        this.dnaSamples = dnaSamples;
    }
    /**
     * @return the preservedSpecimensWithSpecimenScan
     */
    public List<DerivedUnit> getPreservedSpecimensWithSpecimenScan() {
        return preservedSpecimensWithSpecimenScan;
    }
    /**
     * @param preservedSpecimensWithSpecimenScan the preservedSpecimensWithSpecimenScan to set
     */
    public void setPreservedSpecimensWithSpecimenScan(List<DerivedUnit> preservedSpecimensWithSpecimenScan) {
        this.preservedSpecimensWithSpecimenScan = preservedSpecimensWithSpecimenScan;
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
     * @return the isType
     */
    public boolean isType() {
        return isType;
    }
    /**
     * @param isType the isType to set
     */
    public void setType(boolean isType) {
        this.isType = isType;
    }
    /**
     * @return the hasSpecimenScan
     */
    public boolean isHasSpecimenScan() {
        return hasSpecimenScan;
    }
    /**
     * @param hasSpecimenScan the hasSpecimenScan to set
     */
    public void setHasSpecimenScan(boolean hasSpecimenScan) {
        this.hasSpecimenScan = hasSpecimenScan;
    }
    /**
     * @return the numberOfDerivates
     */
    public int getNumberOfDerivates() {
        return numberOfDerivates;
    }
    /**
     * @param numberOfDerivates the numberOfDerivates to set
     */
    public void setNumberOfDerivates(int numberOfDerivates) {
        this.numberOfDerivates = numberOfDerivates;
    }
    /**
     * @return the herbaria
     */
    public List<String> getHerbaria() {
        return herbaria;
    }
    /**
     * @param herbaria the herbaria to set
     */
    public void setHerbaria(List<String> herbaria) {
        this.herbaria = herbaria;
    }
    /**
     * @return the fieldUnit
     */
    public FieldUnit getFieldUnit() {
        return fieldUnit;
    }
    /**
     * @param fieldUnit the fieldUnit to set
     */
    public void setFieldUnit(FieldUnit fieldUnit) {
        this.fieldUnit = fieldUnit;
    }

}
