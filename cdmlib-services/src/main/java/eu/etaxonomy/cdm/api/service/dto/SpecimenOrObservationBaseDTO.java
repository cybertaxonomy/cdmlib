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
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
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
    private String collectorsString;
    private String individualCount;
    private Set<DerivedUnitDTO> derivatives;

    private Set<SpecimenTypeDesignationDTO> specimenTypeDesignations;

    private EventDTO<DerivationEvent> derivationEvent;

    // TODO use DTO !!!
    private Set<IdentifiableSource> sources;

    private List<MediaDTO> listOfMedia = new ArrayList<>();

    private DefinedTerm sex;

    private DefinedTerm lifeStage;

    private List<DeterminationEventDTO>determinations;

    protected SpecimenOrObservationBaseDTO(SpecimenOrObservationBase<?> specimenOrObservation) {
        super(HibernateProxyHelper.getClassWithoutInitializingProxy(specimenOrObservation), specimenOrObservation.getUuid(), specimenOrObservation.getTitleCache());
        this.id = specimenOrObservation.getId();
        Set<Media> collectedMedia = collectMedia(specimenOrObservation);
        addMediaAsDTO(collectedMedia);
        setKindOfUnit(specimenOrObservation.getKindOfUnit());
        setSex(specimenOrObservation.getSex());
        setIndividualCount(specimenOrObservation.getIndividualCount());
        lifeStage = specimenOrObservation.getLifeStage();
        FieldUnit fieldUnit = null;
        if (specimenOrObservation instanceof FieldUnit){
            fieldUnit = (FieldUnit)specimenOrObservation;
        }else{
            fieldUnit = getFieldUnit((DerivedUnit)specimenOrObservation);
        }
        if (fieldUnit != null){
            AgentBase<?> collector = null;
            if (fieldUnit.getGatheringEvent() != null){
                collector = fieldUnit.getGatheringEvent().getCollector();
            }
            String fieldNumberString = CdmUtils.Nz(fieldUnit.getFieldNumber());
            if (collector != null){
                if (collector.isInstanceOf(TeamOrPersonBase.class)){
                    collectorsString = CdmBase.deproxy(collector, TeamOrPersonBase.class).getCollectorTitleCache();
                }else{
                    collectorsString = collector.getTitleCache();  //institutions
                }
            }
            collectorsString = CdmUtils.concat(" - ", collectorsString, fieldNumberString);
        }
        setDeterminations(specimenOrObservation.getDeterminations().stream()
                .map(det -> DeterminationEventDTO.from(det))
                .collect(Collectors.toList())
                );
        if (specimenOrObservation instanceof DerivedUnit){
            DerivedUnit derivedUnit = (DerivedUnit)specimenOrObservation;
            if (derivedUnit.getSpecimenTypeDesignations() != null){
                setSpecimenTypeDesignations(derivedUnit.getSpecimenTypeDesignations());
            }
        }
    }

    /**
     * finds the field unit of the derived unit or null if no field unit exist
     * @param specimenOrObservation
     * @return
     */
    private FieldUnit getFieldUnit(DerivedUnit specimenOrObservation) {
        if (specimenOrObservation.getDerivedFrom() != null && !specimenOrObservation.getDerivedFrom().getOriginals().isEmpty()){
            for (SpecimenOrObservationBase<?> specimen: specimenOrObservation.getDerivedFrom().getOriginals()){
                if (specimen instanceof FieldUnit){
                    return (FieldUnit)specimen;
                }else if (specimen instanceof DerivedUnit){
                    getFieldUnit(HibernateProxyHelper.deproxy(specimen,DerivedUnit.class));
                }
            }
        }
        return null;
    }

    public String getCollectorsString() {
        return collectorsString;
    }
    public void setCollectorsString(String collectorsString) {
        this.collectorsString = collectorsString;
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

    public DerivationTreeSummaryDTO getDerivationTreeSummary() {
        return derivationTreeSummary;
    }
    public void setDerivationTreeSummary(DerivationTreeSummaryDTO derivationTreeSummary) {
        this.derivationTreeSummary = derivationTreeSummary;
        if(derivationTreeSummary != null) {
            setHasSpecimenScan(isHasSpecimenScan() || !derivationTreeSummary.getSpecimenScans().isEmpty());
            setHasDetailImage(isHasDetailImage() || !derivationTreeSummary.getDetailImages().isEmpty());
            setHasDna(isHasDna() || !derivationTreeSummary.getMolecularDataList().isEmpty());
        }
    }

    public TreeSet<AbstractMap.SimpleEntry<String, String>> getCharacterData() {
        return characterData;
    }
    public void addCharacterData(String character, String state){
      if(characterData==null){
          characterData = new TreeSet<>(new PairComparator());
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

    public boolean isHasCharacterData() {
        return hasCharacterData;
    }
    public void setHasCharacterData(boolean hasCharacterData) {
        this.hasCharacterData = hasCharacterData;
    }

    public boolean isHasDna() {
        return hasDna;
    }
    public void setHasDna(boolean hasDna) {
        this.hasDna = hasDna;
    }

    public boolean isHasDetailImage() {
        return hasDetailImage;
    }
    public void setHasDetailImage(boolean hasDetailImage) {
        this.hasDetailImage = hasDetailImage;
    }

    public boolean isHasSpecimenScan() {
        return hasSpecimenScan;
    }
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

    public Set<DerivedUnitDTO> getDerivatives() {
        if (this.derivatives == null){
            this.derivatives = new HashSet<>();
        }
        return derivatives;
    }

    public void setDerivatives(Set<DerivedUnitDTO> derivatives) {
        this.derivatives = derivatives;
        updateTreeDependantData();
    }

    public void addDerivative(DerivedUnitDTO derivate){
        if (this.derivatives == null){
            this.derivatives = new HashSet<>();
        }
        this.derivatives.add(derivate);
        updateTreeDependantData();
    }
    public void addAllDerivatives(Set<DerivedUnitDTO> derivatives){
        if (this.derivatives == null){
            this.derivatives = new HashSet<>();
        }
        this.derivatives.addAll(derivatives);
        updateTreeDependantData();
    }

    /**
     * To be overwritten by implementing classes to
     * update data which depends on the derivation tree
     */
    protected abstract void updateTreeDependantData();

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

    public EventDTO<DerivationEvent> getDerivationEvent() {
        return derivationEvent;
    }
    public void setDerivationEvent(EventDTO<DerivationEvent> derivationEvent) {
        this.derivationEvent = derivationEvent;
    }

    public List<MediaDTO> getListOfMedia() {
        return listOfMedia;
    }
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
            List<MediaDTO> dtos = MediaDTO.fromEntity(m);
            getListOfMedia().addAll(dtos);
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
     * @return
     */
    protected Set<DerivedUnitDTO> assembleDerivatives(SpecimenOrObservationBase<?> sob,
            Integer maxDepth, EnumSet<SpecimenOrObservationType> includeTypes) {

        boolean doDescend = maxDepth == null || maxDepth > 0;
        Integer nextLevelMaxDepth = maxDepth != null ? maxDepth - 1 : null;
        Set<DerivedUnitDTO> derivateDTOs = new HashSet<>();
        // collectDerivedUnitsMaxdepth => 0 to avoid aggregation of sub ordinate
        // derivatives at each level
        Integer collectDerivedUnitsMaxdepth = 0;
        for (DerivedUnit derivedUnit : sob.collectDerivedUnits(collectDerivedUnitsMaxdepth)) {
            if(!derivedUnit.isPublish()){
                continue;
            }

            if (doDescend && (includeTypes == null || includeTypes.contains(derivedUnit.getRecordBasis()))) {
                DerivedUnitDTO derivedUnitDTO = DerivedUnitDTO.fromEntity(derivedUnit, nextLevelMaxDepth, includeTypes);
                derivateDTOs.add(derivedUnitDTO);
                setHasCharacterData(isHasCharacterData() || derivedUnitDTO.isHasCharacterData());
                // NOTE! the flags setHasDetailImage, setHasDna, setHasSpecimenScan are also set in
                // setDerivateDataDTO(), see below
                setHasDetailImage(isHasDetailImage() || derivedUnitDTO.isHasDetailImage());
                setHasDna(isHasDna() || derivedUnitDTO.isHasDna());
                setHasSpecimenScan(isHasSpecimenScan() || derivedUnitDTO.isHasSpecimenScan());
            }
        }
        return derivateDTOs;
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

    public String getIndividualCount() {
        return individualCount;
    }
    public void setIndividualCount(String individualCount) {
        this.individualCount = individualCount;
    }

    public List<DeterminationEventDTO> getDeterminations() {
        return determinations;
    }

    public void setDeterminations(List<DeterminationEventDTO> determinations) {
        this.determinations = determinations;
    }
}