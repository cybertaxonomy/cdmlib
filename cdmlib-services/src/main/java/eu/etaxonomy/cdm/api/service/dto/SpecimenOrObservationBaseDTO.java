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
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;


public abstract class SpecimenOrObservationBaseDTO extends TypedEntityReference<SpecimenOrObservationBase<?>>{

    private static final long serialVersionUID = -7597690654462090732L;

    private int id;
    private TreeSet<AbstractMap.SimpleEntry<String, String>> characterData;
    private DerivationTreeSummaryDTO derivationTreeSummary;
    protected String taxonName;

    protected String summaryLabel;
    protected boolean hasDetailImage;
    private boolean hasCharacterData;
    private boolean hasDna;
    private boolean hasSpecimenScan;

    private SpecimenOrObservationType recordBase;
    private TermBase kindOfUnit;
    private CollectionDTO collection;
    private String catalogNumber;
    private String collectorsNumber;
    private String barcode;
    private String preservationMethod;
    private Set<DerivedUnitDTO> derivatives;

    private Set<SpecimenTypeDesignationDTO> specimenTypeDesignations;

    private DerivationEventDTO derivationEvent;

    // TODO use DTO !!!
    private Set<IdentifiableSource> sources;
    private List<MediaDTO> listOfMedia = new ArrayList<>();

    private DefinedTerm sex;

    private DefinedTerm lifeStage;

    protected SpecimenOrObservationBaseDTO(SpecimenOrObservationBase<?> specimenOrObservation) {
        super(HibernateProxyHelper.getClassWithoutInitializingProxy(specimenOrObservation), specimenOrObservation.getUuid(), specimenOrObservation.getTitleCache());
        this.id = specimenOrObservation.getId();
        Set<Media> collectedMedia = collectMedia(specimenOrObservation);
        addMediaAsDTO(collectedMedia);
        setKindOfUnit(specimenOrObservation.getKindOfUnit());
        setSex(specimenOrObservation.getSex());
        lifeStage = specimenOrObservation.getLifeStage();
        if (specimenOrObservation instanceof DerivedUnit){
            DerivedUnit derivedUnit = (DerivedUnit)specimenOrObservation;
            if (derivedUnit.getSpecimenTypeDesignations() != null){
                setSpecimenTypeDesignations(derivedUnit.getSpecimenTypeDesignations());
            }
        }
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
                this.specimenTypeDesignations.add(new SpecimenTypeDesignationDTO(typeDes));
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
    public DerivationTreeSummaryDTO getDerivationTreeSummary() {
        return derivationTreeSummary;
    }

    /**
     * @param derivationTreeSummary the derivateDataDTO to set
     */
    public void setDerivationTreeSummary(DerivationTreeSummaryDTO derivationTreeSummary) {
        this.derivationTreeSummary = derivationTreeSummary;
        if(derivationTreeSummary != null) {
            setHasSpecimenScan(isHasSpecimenScan() || !derivationTreeSummary.getSpecimenScans().isEmpty());
            setHasDetailImage(isHasDetailImage() || !derivationTreeSummary.getDetailImages().isEmpty());
            setHasDna(isHasDna() || !derivationTreeSummary.getMolecularDataList().isEmpty());
        }
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
     * @return The summary of all DerivedUnit identifiers with the label of
     * this SpecimenOrObservationBase.
     * This label is usually being user for citing the unit in publications.
     */
    public String getSummaryLabel() {
        return summaryLabel;
    }

    /**
     * Summary of all DerivedUnit identifiers with the label of this SpecimenOrObservationBase.
     * This label is usually being user for citing the unit in publications.
     */
    public void setSummaryLabel(String summaryLabel) {
        this.summaryLabel = summaryLabel;
    }


    public SpecimenOrObservationType getRecordBase() {
        return recordBase;
    }
    public void setRecordBase(SpecimenOrObservationType specimenOrObservationType) {
        this.recordBase = specimenOrObservationType;
    }

    /**
     * @return the collection
     *
     * @deprecated TODO remove as it only duplicates the information contained in the collectionDTO
     */
    @Deprecated
    public String getCollectionCode() {
        if (collection != null){
            return collection.getCode();
        } else {
            return null;
        }
    }

    /**
     * @return the collection
     */
    public CollectionDTO getCollection() {
        return collection;
    }
    /**
     * @param collection the collection to set
     */
    public void setCollectioDTO(CollectionDTO collection) {
        this.collection = collection;
    }



    public Set<DerivedUnitDTO> getDerivatives() {
        if (this.derivatives == null){
            this.derivatives = new HashSet<>();
        }
        return derivatives;
    }

    public void setDerivatives(Set<DerivedUnitDTO> derivatives) {
        this.derivatives = derivatives;
    }

    public void addDerivative(DerivedUnitDTO derivate){
        if (this.derivatives == null){
            this.derivatives = new HashSet<>();
        }
        this.derivatives.add(derivate);
    }
    public void addAllDerivatives(Set<DerivedUnitDTO> derivatives){
        if (this.derivatives == null){
            this.derivatives = new HashSet<>();
        }
        this.derivatives.addAll(derivatives);
    }

    /**
     * Recursively collects all derivatives from this.
     */
    public Collection<DerivedUnitDTO> collectDerivatives() {
        return collectDerivatives(new HashSet<>());
    }

    /**
     * private partner method to {@link #collectDerivatives()} for recursive calls.
     *
     * @param dtos
     */
    private Collection<DerivedUnitDTO> collectDerivatives(Set<DerivedUnitDTO> dtos) {
        dtos.addAll(getDerivatives());
        if(derivatives != null) {
            for(SpecimenOrObservationBaseDTO subDto : derivatives) {
                dtos.addAll(subDto.collectDerivatives(dtos));
            }
        }
        return dtos;
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


    protected Set<Media> collectMedia(SpecimenOrObservationBase<?> specimenOrObservation){
        Set<Media> collectedMedia = new HashSet<>();
        Set<SpecimenDescription> descriptions = specimenOrObservation.getSpecimenDescriptionImageGallery();
        for (DescriptionBase<?> desc : descriptions){
            if (desc instanceof SpecimenDescription){
                SpecimenDescription specimenDesc = (SpecimenDescription)desc;
                for (DescriptionElementBase element : specimenDesc.getElements()){
                    if (element.isInstanceOf(TextData.class)&& element.getFeature().equals(Feature.IMAGE())) {
                        for (Media media :element.getMedia()){
                            collectedMedia.add(media);
                        }
                    }
                }
            }
        }
        return collectedMedia;
    }

    private void addMediaAsDTO(Set<Media> media) {
        for(Media m : media) {
            m.getAllTitles(); // initialize all titles!!!
            for (MediaRepresentation rep :m.getRepresentations()){
                for(MediaRepresentationPart p : rep.getParts()){
                    if(p.getUri() != null){
                        MediaDTO dto = new MediaDTO(m.getUuid());
                        dto.setUri(p.getUri().toString());
                        getListOfMedia().add(dto);
                    }
                }
            }
        }
    }

    /**
     * @param sob
     *      The Unit to assemble the derivatives information for
     * @param maxDepth
     *   The maximum number of derivation events levels up to which derivatives are to be assembled.
     *   <code>NULL</code> means infinitely.
     * @param includeTypes
     *      Allows for positive filtering by {@link SpecimenOrObservationType}.
     *      Filter is disabled when <code>NULL</code>. This only affects the derivatives assembled in the
     *      {@link #derivatives} list. The <code>unitLabelsByCollection</code> are always collected for the
     *      whole bouquet of derivatives.
     * @param unitLabelsByCollection
     *      A map to record the unit labels (most significant identifier + collection code) per collection.
     *      Optional parameter, may be <code>NULL</code>.
     */
    protected void assembleDerivatives(SpecimenOrObservationBase<?> sob,
            Integer maxDepth, EnumSet<SpecimenOrObservationType> includeTypes,
            Map<eu.etaxonomy.cdm.model.occurrence.Collection, List<String>> unitLabelsByCollection) {

        boolean doDescend = maxDepth == null || maxDepth > 0;
        Integer nextLevelMaxDepth = maxDepth != null ? maxDepth - 1 : null;
        for (DerivedUnit derivedUnit : sob.collectDerivedUnits()) {
            if(!derivedUnit.isPublish()){
                continue;
            }

            if(unitLabelsByCollection != null) {
                // collect accession numbers for citation
                // collect collections for herbaria column
                eu.etaxonomy.cdm.model.occurrence.Collection collection = derivedUnit.getCollection();
                if (collection != null) {
                    //combine collection with identifier
                    String identifier = derivedUnit.getMostSignificantIdentifier();
                    if (identifier != null && collection.getCode()!=null) {
                        identifier = (collection.getCode()!=null?collection.getCode():"[no collection]")+" "+identifier;
                    }
                    if(!unitLabelsByCollection.containsKey(collection)) {
                        unitLabelsByCollection.put(collection, new ArrayList<>());
                    }
                    unitLabelsByCollection.get(collection).add(identifier);
                }
            }

            if (doDescend && (includeTypes == null || includeTypes.contains(derivedUnit.getRecordBasis()))) {
                DerivedUnitDTO derivedUnitDTO = DerivedUnitDTO.fromEntity(derivedUnit, nextLevelMaxDepth, includeTypes, null);
                addDerivative(derivedUnitDTO);
                setHasCharacterData(isHasCharacterData() || derivedUnitDTO.isHasCharacterData());
                // NOTE! the flags setHasDetailImage, setHasDna, setHasSpecimenScan are also set in
                // setDerivateDataDTO(), see below
                setHasDetailImage(isHasDetailImage() || derivedUnitDTO.isHasDetailImage());
                setHasDna(isHasDna() || derivedUnitDTO.isHasDna());
                setHasSpecimenScan(isHasSpecimenScan() || derivedUnitDTO.isHasSpecimenScan());
            }
        }
    }

    public TermBase getKindOfUnit() {
        return kindOfUnit;
    }
    public void setKindOfUnit(TermBase kindOfUnit) {
        this.kindOfUnit = HibernateProxyHelper.deproxy(kindOfUnit);
    }
    public DefinedTerm getSex() {
        return sex;
    }
    public void setSex(DefinedTerm sex) {
        this.sex = sex;
    }
    public DefinedTerm getLifeStage() {
        return lifeStage;
    }
    public void setLifeStage(DefinedTerm lifeStage) {
        this.lifeStage = lifeStage;
    }


    public int getId() {
        return id;
    }


}
