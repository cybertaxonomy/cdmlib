/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SequenceString;
import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author k.luther
 * @since 02.08.2018
 */
public class SequenceDTO implements Serializable{

    private static final long serialVersionUID = 4532272104416494517L;

    private Media contigFile;
    private SequenceString consensusSequence;
    private Boolean isBarcode = null;
    private SequenceString barcodeSequencePart = SequenceString.NewInstance();
    private String geneticAccessionNumber;
    private String boldProcessId;
    private Set<SingleReadAlignment> singleReadAlignments = new HashSet<SingleReadAlignment>();
    private String dnaMarker;
    private String haplotype;
    private Set<Reference> citations = new HashSet<>();

    private URI boldUri;
    private URI ddbjUri;
    private URI emblUri;
    private URI genBankUri;

    public SequenceDTO(Sequence seq){
        contigFile = seq.getContigFile();
        consensusSequence = seq.getConsensusSequence();
        isBarcode = seq.getIsBarcode();
        barcodeSequencePart = seq.getBarcodeSequencePart();
        geneticAccessionNumber = seq.getGeneticAccessionNumber();
        boldProcessId = seq.getBoldProcessId();
        singleReadAlignments = seq.getSingleReadAlignments();
        if (seq.getDnaMarker() != null){
            dnaMarker = seq.getDnaMarker().getLabel();
        }
        haplotype = seq.getHaplotype();
        citations = seq.getCitations();
        try{
            boldUri = seq.getBoldUri();
            ddbjUri = seq.getDdbjUri();
            emblUri = seq.getEmblUri();
            genBankUri = seq.getGenBankUri();
        } catch (URISyntaxException e){
//TODO
        }
    }

    public URI getBoldUri() {
        return boldUri;
    }

    public URI getDdbjUri() {
        return ddbjUri;
    }

    public URI getEmblUri() {
        return emblUri;
    }

    public URI getGenBankUri() {
        return genBankUri;
    }

    public Media getContigFile() {
        return contigFile;
    }

    public SequenceString getConsensusSequence() {
        return consensusSequence;
    }

    public Boolean getIsBarcode() {
        return isBarcode;
    }

    public SequenceString getBarcodeSequencePart() {
        return barcodeSequencePart;
    }

    public String getGeneticAccessionNumber() {
        return geneticAccessionNumber;
    }

    public String getBoldProcessId() {
        return boldProcessId;
    }

    public Set<SingleReadAlignment> getSingleReadAlignments() {
        return singleReadAlignments;
    }

    public String getDnaMarker() {
        return dnaMarker;
    }

    public String getHaplotype() {
        return haplotype;
    }

    public Set<Reference> getCitations() {
        return citations;
    }
}