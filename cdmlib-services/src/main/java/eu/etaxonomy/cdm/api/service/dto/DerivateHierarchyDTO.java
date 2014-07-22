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
	private List<String> herbaria;
	private FieldUnit fieldUnit;

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
