/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.nameMatching;

/**
 * A name matching result representing a single name that
 * does not exactly match a name. So it is a candiate only.
 * The given distance parameter defines the distance between
 * the requested name parameters and the returned name.
 *
 * @author muellera
 * @since 03.04.2024
 */
public class NameMatchingCandidateResult extends NameMatchingExactResult {

    private Double matchingScore;

    public Double getMatchingScore() {
        return matchingScore;
    }
    public void setMatchingScore(Double matchingScore) {
        this.matchingScore = matchingScore;
    }
}