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
    private List<NameMatchingCandidateResult> candidates = new ArrayList<>();

    public List<NameMatchingExactResult> getExactMatches() {
        return exactMatches;
    }
    public void setExactMatches(List<NameMatchingExactResult> exactMatches) {
        this.exactMatches = exactMatches;
    }

    public List<NameMatchingCandidateResult> getCandidates() {
        return candidates;
    }
    public void setCandidates(List<NameMatchingCandidateResult> candidates) {
        this.candidates = candidates;
    }

}