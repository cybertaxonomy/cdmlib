/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;


public abstract class SpecimenOrObservationBaseDTO<T extends SpecimenOrObservationBase<?>>
        extends TypedEntityReference<T>{

    private static final long serialVersionUID = -7597690654462090732L;

    private int id;
    private TreeSet<AbstractMap.SimpleEntry<String, String>> characterData = new TreeSet<>(new PairComparator());

    //computed summary data
    private DerivationTreeSummaryDTO derivationTreeSummary;
    protected String summaryLabel;
    protected boolean hasDetailImage;
    private boolean hasCharacterData;
    private boolean hasDna;
    private boolean hasSpecimenScan;

    //occurrence data
    private String recordBase;
    private DefinedTermDTO kindOfUnit;
    private String individualCount;
    private Set<DerivedUnitDTO> derivatives = new HashSet<>();
    private Set<AnnotationDTO> annotations = new HashSet<>();
    private Set<SourceDTO> sources;
    private DefinedTermDTO sex;
    private DefinedTermDTO lifeStage;
    private List<DeterminationEventDTO>determinations;

    //TODO is this needed here or in FieldUnitDTO, references the field unit information
    private String collectorsString;

    //links to this specimen
    private Set<SpecimenTypeDesignationDTO> specimenTypeDesignations = new HashSet<>();

    private List<MediaDTO> listOfMedia = new ArrayList<>();

    // ************************* CONSTRUCTOR *************************/

    public SpecimenOrObservationBaseDTO(Class<T> type, UUID uuid, String label) {
        super(type, uuid, label);
    }

    // ************************ GETTER / SETTER **********************/

    public String getCollectorsString() {
        return collectorsString;
    }
    public void setCollectorsString(String collectorsString) {
        this.collectorsString = collectorsString;
    }

    public Set<SpecimenTypeDesignationDTO> getSpecimenTypeDesignations() {
        return specimenTypeDesignations;
    }
    public void addSpecimenTypeDesignation(SpecimenTypeDesignationDTO typeDto) {
        this.specimenTypeDesignations.add(typeDto);
    }

    public Set<SourceDTO> getSources() {
        return sources;
    }
    public void setSources(Set<SourceDTO> sources) {
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
        characterData.add(new AbstractMap.SimpleEntry<>(character, state));
        this.setHasCharacterData(!this.characterData.isEmpty());
    }

    public boolean isHasCharacterData() {
        return hasCharacterData;
    }
    protected void setHasCharacterData(boolean hasCharacterData) {
        this.hasCharacterData = hasCharacterData;
    }

    public boolean isHasDna() {
        return hasDna;
    }
    protected void setHasDna(boolean hasDna) {
        this.hasDna = hasDna;
    }

    public boolean isHasDetailImage() {
        return hasDetailImage;
    }
    protected void setHasDetailImage(boolean hasDetailImage) {
        this.hasDetailImage = hasDetailImage;
    }

    public boolean isHasSpecimenScan() {
        return hasSpecimenScan;
    }
    protected void setHasSpecimenScan(boolean hasSpecimenScan) {
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
     * This label is usually being used for citing the unit in publications.
     */
    public void setSummaryLabel(String summaryLabel) {
        this.summaryLabel = summaryLabel;
    }

    public String getRecordBase() {
        return recordBase;
    }
    public void setRecordBase(String specimenOrObservationType) {
        this.recordBase = specimenOrObservationType;
    }

    public Set<DerivedUnitDTO> getDerivatives() {
        return derivatives;
    }

    public void addDerivative(DerivedUnitDTO derivate){
        this.derivatives.add(derivate);
        Set<DerivedUnitDTO> derivatives = new HashSet<>();
        derivatives.add(derivate);
        updateTreeDependantData(derivatives);
    }

    public void addAllDerivatives(Set<DerivedUnitDTO> derivatives){
        this.derivatives.addAll(derivatives);
        updateTreeDependantData(derivatives);
    }

    public List<MediaDTO> getListOfMedia() {
        return listOfMedia;
    }
    public void setListOfMedia(List<MediaDTO> listOfMedia) {
        this.listOfMedia = listOfMedia;
    }

    public DefinedTermDTO getKindOfUnit() {
        return kindOfUnit;
    }
    public void setKindOfUnit(DefinedTermDTO kindOfUnit) {
        this.kindOfUnit = kindOfUnit;
    }

    public DefinedTermDTO getSex() {
        return sex;
    }
    public void setSex(DefinedTermDTO sex) {
        this.sex = sex;
    }

    public DefinedTermDTO getLifeStage() {
        return lifeStage;
    }
    public void setLifeStage(DefinedTermDTO lifeStage) {
        this.lifeStage = lifeStage;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
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

    public Set<AnnotationDTO> getAnnotations() {
        return annotations;
    }
    public void addAnnotation(AnnotationDTO annotation) {
        this.annotations.add(annotation);
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
     * To be overwritten by implementing classes to
     * update data which depends on the derivation tree
     */
    protected void updateTreeDependantData(Set<DerivedUnitDTO> derivatives) {
        for (DerivedUnitDTO derivative: derivatives) {
            setHasDna(isHasDna() || derivative.isHasDna() || !derivative.getDerivationTreeSummary().getMolecularDataList().isEmpty());
            setHasDetailImage(isHasDetailImage() || derivative.isHasDetailImage() || !derivative.getDerivationTreeSummary().getDetailImages().isEmpty());
            setHasSpecimenScan(isHasSpecimenScan()|| derivative.isHasSpecimenScan());
            setHasCharacterData(isHasCharacterData()||derivative.isHasCharacterData());
        }
    }

}