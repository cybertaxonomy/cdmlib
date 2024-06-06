/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import eu.etaxonomy.cdm.model.molecular.DnaQuality;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author muellera
 * @since 17.02.2024
 */
public class DnaQualityDTO extends TypedEntityReference<DnaQuality> {

//    private MaterialOrMethodEvent typedPurificationMethod;

    private String purificationMethod;

    private Double ratioOfAbsorbance260_230;

    private Double ratioOfAbsorbance260_280;

    private Double concentration;

    private String concentrationUnit;

    private String qualityTerm;

    private LocalDateTime qualityCheckDate;

    public DnaQualityDTO(Class<DnaQuality> type, UUID uuid, String label) {
        super(type, uuid, label);
    }


//    public MaterialOrMethodEvent getTypedPurificationMethod() {
//        return typedPurificationMethod;
//    }
//    public void setTypedPurificationMethod(MaterialOrMethodEvent typedPurificationMethod) {
//        this.typedPurificationMethod = typedPurificationMethod;
//    }

    public String getPurificationMethod() {
        return purificationMethod;
    }
    public void setPurificationMethod(String purificationMethod) {
        this.purificationMethod = purificationMethod;
    }

    public Double getRatioOfAbsorbance260_230() {
        return ratioOfAbsorbance260_230;
    }
    public void setRatioOfAbsorbance260_230(Double ratioOfAbsorbance260_230) {
        this.ratioOfAbsorbance260_230 = ratioOfAbsorbance260_230;
    }

    public Double getRatioOfAbsorbance260_280() {
        return ratioOfAbsorbance260_280;
    }
    public void setRatioOfAbsorbance260_280(Double ratioOfAbsorbance260_280) {
        this.ratioOfAbsorbance260_280 = ratioOfAbsorbance260_280;
    }

    public Double getConcentration() {
        return concentration;
    }
    public void setConcentration(Double concentration) {
        this.concentration = concentration;
    }

    public String getConcentrationUnit() {
        return concentrationUnit;
    }
    public void setConcentrationUnit(String concentrationUnit) {
        this.concentrationUnit = concentrationUnit;
    }

    public String getQualityTerm() {
        return qualityTerm;
    }
    public void setQualityTerm(String qualityTerm) {
        this.qualityTerm = qualityTerm;
    }

    public LocalDateTime getQualityCheckDate() {
        return qualityCheckDate;
    }
    public void setQualityCheckDate(LocalDateTime qualityCheckDate) {
        this.qualityCheckDate = qualityCheckDate;
    }
}
