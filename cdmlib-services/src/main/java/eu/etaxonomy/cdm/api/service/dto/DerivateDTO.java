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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;


/**
 * @author pplitzner
 * @since Mar 27, 2015
 */
public abstract class DerivateDTO extends TypedEntityReference{

    private static final long serialVersionUID = -7597690654462090732L;

    private TreeSet<AbstractMap.SimpleEntry<String, String>> characterData;
    private DerivateDataDTO derivateDataDTO;
    protected String taxonName;
    protected String listLabel;

    protected String citation;
    protected boolean hasDetailImage;
    private boolean hasCharacterData;
    private boolean hasDna;
    private boolean hasSpecimenScan;

    private String recordBase;
    private String kindOfUnit;
    private CollectionDTO collection;
    private String catalogNumber;
    private String collectorsNumber;
    private String barcode;
    private String preservationMethod;
    private Set<DerivateDTO> derivates;

    private Set<SpecimenTypeDesignationDTO> specimenTypeDesignations;

    private DerivationEventDTO derivationEvent;

    private Set<IdentifiableSource> sources;
    private List<MediaDTO> listOfMedia = new ArrayList<>();

    /**
     * Factory method to create a new instance of the passed <code>SpecimenOrObservationBase</code> with the matching sub
     * type of <code>DerivateDTO</code>
     *
     * @param sob
     * @return
     */
    public static DerivateDTO newInstance(SpecimenOrObservationBase sob){
        DerivateDTO derivateDto;
        if (sob.isInstanceOf(FieldUnit.class)){
            derivateDto = FieldUnitDTO.newInstance(sob);
        } else if (sob instanceof DnaSample){
            derivateDto = new DNASampleDTO((DnaSample)sob);
        } else {
            derivateDto = new PreservedSpecimenDTO((DerivedUnit)sob);
        }
        return derivateDto;
    }

    public DerivateDTO(SpecimenOrObservationBase specimenOrObservation) {
        super(specimenOrObservation.getClass(), specimenOrObservation.getUuid(), specimenOrObservation.getTitleCache());
        addMedia(specimenOrObservation);
        if (specimenOrObservation.getKindOfUnit() != null){
            setKindOfUnit(specimenOrObservation.getKindOfUnit().getTitleCache());
        }
        if (specimenOrObservation instanceof DerivedUnit){
            DerivedUnit derivedUnit = (DerivedUnit)specimenOrObservation;
            if (derivedUnit.getSpecimenTypeDesignations() != null){
                setSpecimenTypeDesignations(derivedUnit.getSpecimenTypeDesignations());
            }
        }


    }
    public String getTitleCache() {
        return getLabel();
    }


    public String getListLabel() {
        return listLabel;
    }

    public void setListLabel(String listLabel) {
        this.listLabel = listLabel;
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

    public Set<SpecimenTypeDesignationDTO> getSpecimenTypeDesignations() {
        return specimenTypeDesignations;
    }

    public void setSpecimenTypeDesignations(Set<SpecimenTypeDesignation> specimenTypeDesignations) {
        this.specimenTypeDesignations = new HashSet<>();
        for (SpecimenTypeDesignation typeDes: specimenTypeDesignations){
            if (typeDes != null){
                this.specimenTypeDesignations.add(new SpecimenTypeDesignationDTO(typeDes, this));
            }
        }

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
    public TreeSet<AbstractMap.SimpleEntry<String, String>> getCharacterData() {
        return characterData;
    }

    public void addCharacterData(String character, String state){
      if(characterData==null){
          characterData = new TreeSet<AbstractMap.SimpleEntry<String,String>>(new PairComparator());
      }
      characterData.add(new AbstractMap.SimpleEntry<>(character, state));
    }

    private class PairComparator implements Comparator<AbstractMap.SimpleEntry<String,String>>, Serializable {

        private static final long serialVersionUID = -8589392050761963540L;

        @Override
        public int compare(AbstractMap.SimpleEntry<String, String> o1, AbstractMap.SimpleEntry<String, String> o2) {
            if(o1==null && o2!=null){
                return -1;
            }
            if(o1!=null && o2==null){
                return 1;
            }
            if(o1!=null && o2!=null){
                return o1.getKey().compareTo(o2.getKey());
            }
            return 0;
        }
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
    public String getCollection() {
        if (collection != null){
            return collection.getCode();
        } else {
            return null;
        }
    }
    /**
     * @param collection the collection to set
     */
    public void setCollection(String herbarium) {
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


    /**
     * @return the listOfMedia
     */
    public List<MediaDTO> getListOfMedia() {
        return listOfMedia;
    }

    /**
     * @param listOfMedia the listOfMedia to set
     */
    public void setListOfMedia(List<MediaDTO> listOfMedia) {
        this.listOfMedia = listOfMedia;
    }

    public void addMedia(SpecimenOrObservationBase specimenOrObservation){
        Set<DescriptionBase> descriptions = specimenOrObservation.getSpecimenDescriptionImageGallery();
        for (DescriptionBase desc : descriptions){
            if (desc instanceof SpecimenDescription){
                SpecimenDescription specimenDesc = (SpecimenDescription)desc;
                for (DescriptionElementBase element : specimenDesc.getElements()){
                    if (element.isInstanceOf(TextData.class)&& element.getFeature().equals(Feature.IMAGE())) {
                        for (Media media :element.getMedia()){
                            for (MediaRepresentation rep :media.getRepresentations()){
                                for(MediaRepresentationPart p : rep.getParts()){
                                    if(p.getUri() != null){
                                        MediaDTO dto = new MediaDTO(media.getUuid());
                                        dto.setUri(p.getUri().toString());
                                        this.getListOfMedia().add(dto);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public String getKindOfUnit() {
        return kindOfUnit;
    }
    public void setKindOfUnit(String kindOfUnit) {
        this.kindOfUnit = kindOfUnit;
    }


}
