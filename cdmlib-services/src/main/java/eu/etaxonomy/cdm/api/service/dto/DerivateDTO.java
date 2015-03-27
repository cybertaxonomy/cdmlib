// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.tools.Pair;

/**
 * @author pplitzner
 * @date Mar 27, 2015
 *
 */
public abstract class DerivateDTO {

    private boolean hasDna;
    private boolean hasDetailImage;
    private boolean hasCharacterData;
    private List<Pair<String, String>> characterData;
    private DerivateDataDTO derivateDataDTO;
    private boolean hasSpecimenScan;

    /**
     * @return the derivateDataDTO
     */
    public DerivateDataDTO getDerivateDataDTO() {
        return derivateDataDTO;
    }

    /**
     * @param derivateDataDTO the derivateDataDTO to set
     */
    public void setDerivateDataDTO(DerivateDataDTO derivateDataDTO) {
        this.derivateDataDTO = derivateDataDTO;
    }

    /**
     * @return the characterData
     */
    public List<Pair<String, String>> getCharacterData() {
        return characterData;
    }

    public void addCharacterData(String character, String state){
      if(characterData==null){
          characterData = new ArrayList<Pair<String,String>>();
      }
      characterData.add(new Pair<String, String>(character, state));
    }

    /**
     * @return the hasDna
     */
    public boolean isHasDna() {
        return hasDna;
    }

    /**
     * @param hasDna the hasDna to set
     */
    public void setHasDna(boolean hasDna) {
        this.hasDna = hasDna;
    }

    /**
     * @return the hasDetailImage
     */
    public boolean isHasDetailImage() {
        return hasDetailImage;
    }

    /**
     * @param hasDetailImage the hasDetailImage to set
     */
    public void setHasDetailImage(boolean hasDetailImage) {
        this.hasDetailImage = hasDetailImage;
    }

    /**
     * @return the hasCharacterData
     */
    public boolean isHasCharacterData() {
        return hasCharacterData;
    }

    /**
     * @param hasCharacterData the hasCharacterData to set
     */
    public void setHasCharacterData(boolean hasCharacterData) {
        this.hasCharacterData = hasCharacterData;
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


}
