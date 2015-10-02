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

import java.io.Serializable;
import java.util.Comparator;
import java.util.TreeSet;


/**
 * @author pplitzner
 * @date Mar 27, 2015
 *
 */
public abstract class DerivateDTO implements Serializable{

    private static final long serialVersionUID = 373446709259729891L;

    private TreeSet<Pair<String, String>> characterData;
    private DerivateDataDTO derivateDataDTO;
    protected String taxonName;
    protected String citation;
    protected boolean hasDetailImage;
    private boolean hasCharacterData;
    private boolean hasDna;
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
    public TreeSet<Pair<String, String>> getCharacterData() {
        return characterData;
    }

    public void addCharacterData(String character, String state){
      if(characterData==null){
          characterData = new TreeSet<Pair<String,String>>(new Comparator<Pair<String,String>>() {

            @Override
            public int compare(Pair<String, String> o1, Pair<String, String> o2) {
                if(o1==null && o2!=null){
                    return -1;
                }
                if(o1!=null && o2==null){
                    return 1;
                }
                if(o1!=null && o2!=null){
                    return o1.getA().compareTo(o2.getA());
                }
                return 0;
            }
        });
      }
      characterData.add(new Pair<String, String>(character, state));
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
     * @return the citation
     */
    public String getCitation() {
        return citation;
    }
    /**
     * @param citation the citation to set
     */
    public void setCitation(String citation) {
        this.citation = citation;
    }


}
