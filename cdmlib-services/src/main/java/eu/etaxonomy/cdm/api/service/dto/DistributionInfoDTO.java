/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.Set;

import eu.etaxonomy.cdm.api.util.DistributionTree;
import eu.etaxonomy.cdm.model.description.Distribution;

/**
 * @author a.kohlbecker
 * @since Jan 29, 2014
 */
public class DistributionInfoDTO {

    private CondensedDistribution condensedDistribution = null;
    private DistributionTree tree = null;
    private String mapUriParams = null;
    private Set<Distribution> elements = null;


    public CondensedDistribution getCondensedDistribution() {
        return condensedDistribution;
    }
    public void setCondensedDistribution(CondensedDistribution condensedDistribution) {
        this.condensedDistribution = condensedDistribution;
    }

    public DistributionTree getTree() {
        return tree;
    }
    public void setTree(DistributionTree tree) {
        this.tree = tree;
    }

    public String getMapUriParams() {
        return mapUriParams;
    }
    public void setMapUriParams(String mapUriParams) {
        this.mapUriParams = mapUriParams;
    }

    public Set<Distribution> getElements() {
        return elements;
    }    public void setElements(Set<Distribution> elements) {
        this.elements = elements;
    }

    public enum InfoPart{
        condensedDistribution,
        tree,
        mapUriParams,
        elements,
    }
}