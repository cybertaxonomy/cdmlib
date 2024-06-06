/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class AmplificationResultDTO extends TypedEntityReference<AmplificationResult>{

    private static final long serialVersionUID = 1628005396688482355L;

    //amplification data
    private DefinedTermDTO dnaMarker;
    private PrimerDTO forwardPrimer;
    private PrimerDTO reversePrimer;
//    MaterialOrMethodEvent
    private String purification;
    private String institution;
    private String ladderUsed;
    private Double electrophoresisVoltage;
    private Double gelRunningTime;
    private Double gelConcentration;

    public AmplificationResultDTO(Class<AmplificationResult> type, UUID uuid, String label) {
        super(type, uuid, label);
    }

    private Set<SingleReadDTO> singleReads = new HashSet<>();

    //TODO
//    private Cloning cloning;

    private Boolean successful;

    private String successText;

    private MediaDTO gelPhoto;

    public DefinedTermDTO getDnaMarker() {
        return dnaMarker;
    }
    public void setDnaMarker(DefinedTermDTO dnaMarker) {
        this.dnaMarker = dnaMarker;
    }

    public PrimerDTO getForwardPrimer() {
        return forwardPrimer;
    }
    public void setForwardPrimer(PrimerDTO forwardPrimer) {
        this.forwardPrimer = forwardPrimer;
    }

    public PrimerDTO getReversePrimer() {
        return reversePrimer;
    }
    public void setReversePrimer(PrimerDTO reversePrimer) {
        this.reversePrimer = reversePrimer;
    }

    public String getPurification() {
        return purification;
    }
    public void setPurification(String purification) {
        this.purification = purification;
    }

    public String getInstitution() {
        return institution;
    }
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getLadderUsed() {
        return ladderUsed;
    }
    public void setLadderUsed(String ladderUsed) {
        this.ladderUsed = ladderUsed;
    }

    public Double getElectrophoresisVoltage() {
        return electrophoresisVoltage;
    }
    public void setElectrophoresisVoltage(Double electrophoresisVoltage) {
        this.electrophoresisVoltage = electrophoresisVoltage;
    }

    public Double getGelRunningTime() {
        return gelRunningTime;
    }
    public void setGelRunningTime(Double gelRunningTime) {
        this.gelRunningTime = gelRunningTime;
    }

    public Double getGelConcentration() {
        return gelConcentration;
    }
    public void setGelConcentration(Double gelConcentration) {
        this.gelConcentration = gelConcentration;
    }

    public Set<SingleReadDTO> getSingleReads() {
        return singleReads;
    }
    public void addSingleRead(SingleReadDTO singleReadDto) {
        this.singleReads.add(singleReadDto);

    }
    public void setSingleReads(Set<SingleReadDTO> singleReads) {
        this.singleReads = singleReads;
    }

    public Boolean getSuccessful() {
        return successful;
    }
    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public String getSuccessText() {
        return successText;
    }
    public void setSuccessText(String successText) {
        this.successText = successText;
    }

    public MediaDTO getGelPhoto() {
        return gelPhoto;
    }
    public void setGelPhoto(MediaDTO gelPhoto) {
        this.gelPhoto = gelPhoto;
    }
}