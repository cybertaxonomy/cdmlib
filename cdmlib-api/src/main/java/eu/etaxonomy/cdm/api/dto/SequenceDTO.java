/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment;

/**
 * @author k.luther
 * @since 02.08.2018
 */
public class SequenceDTO implements Serializable{

    private static final long serialVersionUID = 2554339091503003103L;

    private MediaDTO contigFile;

    private String consensusSequence;

    private Boolean isBarcode = null;

    private String barcodeSequencePart;

    private String geneticAccessionNumber;

    private String boldProcessId;

    //TODO DTO remove model
    private Set<SingleReadAlignment> singleReadAlignments = new HashSet<>();

    private String dnaMarker;

    private String haplotype;

    private Set<ReferenceDTO> citations = new HashSet<>();

    private URI boldUri;
    private URI ddbjUri;
    private URI emblUri;
    private URI genBankUri;

    // *************** GETTER / SETTER *********************************/

    public URI getBoldUri() {
        return boldUri;
    }
    public void setBoldUri(URI boldUri) {
        this.boldUri = boldUri;
    }

    public URI getDdbjUri() {
        return ddbjUri;
    }
    public void setDdbjUri(URI ddbjUri) {
        this.ddbjUri = ddbjUri;
    }

    public URI getEmblUri() {
        return emblUri;
    }
    public void setEmblUri(URI emblUri) {
        this.emblUri = emblUri;
    }

    public URI getGenBankUri() {
        return genBankUri;
    }
    public void setGenBankUri(URI genBankUri) {
        this.genBankUri = genBankUri;
    }

    public MediaDTO getContigFile() {
        return contigFile;
    }
    public void setContigFile(MediaDTO contigFile) {
        this.contigFile = contigFile;
    }

    public String getConsensusSequence() {
        return consensusSequence;
    }
    public void setConsensusSequence(String consensusSequence) {
        this.consensusSequence = consensusSequence;
    }

    public Boolean getIsBarcode() {
        return isBarcode;
    }
    public void setIsBarcode(Boolean isBarcode) {
        this.isBarcode = isBarcode;
    }

    public String getBarcodeSequencePart() {
        return barcodeSequencePart;
    }
    public void setBarcodeSequencePart(String barcodeSequencePart) {
        this.barcodeSequencePart = barcodeSequencePart;
    }

    public String getGeneticAccessionNumber() {
        return geneticAccessionNumber;
    }
    public void setGeneticAccessionNumber(String geneticAccessionNumber) {
        this.geneticAccessionNumber = geneticAccessionNumber;
    }

    public String getBoldProcessId() {
        return boldProcessId;
    }
    public void setBoldProcessId(String boldProcessId) {
        this.boldProcessId = boldProcessId;
    }

    public Set<SingleReadAlignment> getSingleReadAlignments() {
        return singleReadAlignments;
    }
    public void setSingleReadAlignments(Set<SingleReadAlignment> singleReadAlignments) {
        this.singleReadAlignments = singleReadAlignments;
    }


    public String getDnaMarker() {
        return dnaMarker;
    }
    public void setDnaMarker(String dnaMarker) {
        this.dnaMarker = dnaMarker;
    }

    public String getHaplotype() {
        return haplotype;
    }
    public void setHaplotype(String haplotype) {
        this.haplotype = haplotype;
    }

    public Set<ReferenceDTO> getCitations() {
        return citations;
    }
    public void setCitations(Set<ReferenceDTO> citations) {
        this.citations = citations;
    }
}