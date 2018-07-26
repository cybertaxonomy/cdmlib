/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.hibernate.envers.tools.Pair;

import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;

/**
 * @author pplitzner
 * @since Mar 27, 2015
 *
 */
public abstract class DerivateDTO {

    private TreeSet<Pair<String, String>> characterData;
    private DerivateDataDTO derivateDataDTO;

//    protected String taxonName;

    protected String titleCache;

    protected String citation;
    protected boolean hasDetailImage;
    private boolean hasCharacterData;
    private boolean hasDna;
    private boolean hasSpecimenScan;
    private String recordBase;
    private CollectionDTO collection;
    private String catalogNumber;
    private String collectorsNumber;
    private String barcode;
    private String preservationMethod;
    private Set<DerivateDTO> derivates;
    private UUID uuid;

    private Set<SpecimenTypeDesignation> specimenTypeDesignations;

    private DerivationEventDTO derivationEvent;

    private Set<IdentifiableSource> sources;

    public String getTitleCache() {
        return titleCache;
    }

    public void setTitleCache(String titleCache) {
        this.titleCache = titleCache;
    }

    public void setCollection(CollectionDTO collection) {
        this.collection = collection;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public String getCollectorsNumber() {
        return collectorsNumber;
    }

    public void setCollectorsNumber(String collectorsNumber) {
        this.collectorsNumber = collectorsNumber;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPreservationMethod() {
        return preservationMethod;
    }

    public void setPreservationMethod(String preservationMethod) {
        this.preservationMethod = preservationMethod;
    }

    public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations() {
        return specimenTypeDesignations;
    }

    public void setSpecimenTypeDesignations(Set<SpecimenTypeDesignation> specimenTypeDesignations) {
        this.specimenTypeDesignations = specimenTypeDesignations;
    }


    public Set<IdentifiableSource> getSources() {
        return sources;
    }

    public void setSources(Set<IdentifiableSource> sources) {
        this.sources = sources;
    }

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
                    return o1.getFirst().compareTo(o2.getFirst());
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

    public String getRecordBase() {
        return recordBase;
    }
    public void setRecordBase(String recordBase) {
        this.recordBase = recordBase;
    }

    /**
     * @return the collection
     */
    public String getHerbarium() {
        if (collection != null){
            return collection.getCode();
        } else {
            return null;
        }
    }
    /**
     * @param collection the collection to set
     */
    public void setHerbarium(String herbarium) {
        if (collection == null){
            collection = new CollectionDTO(herbarium, null, null, null);
        }else{
            this.collection.setCode(herbarium);
        }
    }
    /**
     * @return the collection
     */
    public CollectionDTO getCollectionDTO() {
        return collection;
    }
    /**
     * @param collection the collection to set
     */
    public void setCollectioDTo(CollectionDTO collection) {
        this.collection = collection;
    }



    public Set<DerivateDTO> getDerivates() {
        return derivates;
    }

    public void setDerivates(Set<DerivateDTO> derivates) {
        this.derivates = derivates;
    }

    public void addDerivate(DerivateDTO derivate){
        if (this.derivates == null){
            this.derivates = new HashSet<>();
        }
        this.derivates.add(derivate);
    }
    public void addAllDerivates(Set<DerivateDTO> derivates){
        if (this.derivates == null){
            this.derivates = new HashSet<>();
        }
        this.derivates.addAll(derivates);
    }

    public DerivationEventDTO getDerivationEvent() {
        return derivationEvent;
    }

    public void setDerivationEvent(DerivationEventDTO derivationEvent) {
        this.derivationEvent = derivationEvent;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


}
