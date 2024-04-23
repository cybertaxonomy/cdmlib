/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.nameMatching;

import java.util.ArrayList;
import java.util.List;

public class NameMatchingCombinedResult{

    private List<NameMatchingExactResult> exactMatches = new ArrayList<>();
    private List<NameMatchingCandidateResult> closestMatches = new ArrayList<>();
    private List<NameMatchingOtherCandidateResult> otherCandidates = new ArrayList<>();

    public List<NameMatchingExactResult> getExactMatches() {
        return exactMatches;
    }
    public void setExactMatches(List<NameMatchingExactResult> exactMatches) {
        this.exactMatches = exactMatches;
    }

    public List<NameMatchingCandidateResult> getClosestMatches() {
        return closestMatches;
    }
    public void setClosestMatches(List<NameMatchingCandidateResult> closestMatches) {
        this.closestMatches = closestMatches;
    }
    public List<NameMatchingOtherCandidateResult> getOtherCandidates() {
        return otherCandidates;
    }
    public void setOtherCandidates(List<NameMatchingOtherCandidateResult> otherCandidates) {
        this.otherCandidates = otherCandidates;
    }
}