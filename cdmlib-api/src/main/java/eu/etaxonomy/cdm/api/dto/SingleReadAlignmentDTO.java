/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author muellera
 * @since 21.11.2024
 */
public class SingleReadAlignmentDTO extends TypedEntityReference<SingleReadAlignment>{

    private static final long serialVersionUID = -2444011607208110025L;

    //TODO is this needed?
    private SequenceDTO consensusAlignment;

    //TODO is this needed?
    private SingleReadDTO singleRead;

    //probably we do not need the shifts for the dataportal
//    private Shift[] shifts = new Shift[0];

    private Integer firstSeqPosition;

    private Integer leftCutPosition;

    private Integer rightCutPosition;

    private String editedSequence;

    private boolean reverseComplement;

    public SingleReadAlignmentDTO(Class<SingleReadAlignment> type, UUID uuid, String label) {
        super(type, uuid, label);
    }

// ******************** GETTER / SETTER ***********************************/


    public SingleReadDTO getSingleRead() {
        return singleRead;
    }
    public void setSingleRead(SingleReadDTO singleRead) {
        this.singleRead = singleRead;
    }

    public SequenceDTO getConsensusAlignment() {
        return consensusAlignment;
    }
    public void setConsensusAlignment(SequenceDTO consensusAlignment) {
        this.consensusAlignment = consensusAlignment;
    }

    public Integer getFirstSeqPosition() {
        return firstSeqPosition;
    }
    public void setFirstSeqPosition(Integer firstSeqPosition) {
        this.firstSeqPosition = firstSeqPosition;
    }

    public Integer getLeftCutPosition() {
        return leftCutPosition;
    }
    public void setLeftCutPosition(Integer leftCutPosition) {
        this.leftCutPosition = leftCutPosition;
    }

    public Integer getRightCutPosition() {
        return rightCutPosition;
    }
    public void setRightCutPosition(Integer rightCutPosition) {
        this.rightCutPosition = rightCutPosition;
    }

    public String getEditedSequence() {
        return editedSequence;
    }
    public void setEditedSequence(String editedSequence) {
        this.editedSequence = editedSequence;
    }

    public boolean isReverseComplement() {
        return reverseComplement;
    }
    public void setReverseComplement(boolean reverseComplement) {
        this.reverseComplement = reverseComplement;
    }

}