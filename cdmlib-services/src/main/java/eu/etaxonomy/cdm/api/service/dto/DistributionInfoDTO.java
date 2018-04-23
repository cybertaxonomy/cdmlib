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

import eu.etaxonomy.cdm.api.service.DistributionTree;
import eu.etaxonomy.cdm.model.description.Distribution;

/**
 * @author a.kohlbecker
 * @since Jan 29, 2014
 *
 */
public class DistributionInfoDTO {


    private CondensedDistribution condensedDistribution = null;
    private DistributionTree tree = null;
    private String mapUriParams = null;
    private Set<Distribution> elements = null;


    /**
     * @param condensedDistribution the condensedDistribution to set
     */
    public void setCondensedDistribution(CondensedDistribution condensedDistribution) {
        this.condensedDistribution = condensedDistribution;
    }
    /**
     * @param tree the tree to set
     */
    public void setTree(DistributionTree tree) {
        this.tree = tree;
    }
    /**
     * @param mapUriParams the mapUriParams to set
     */
    public void setMapUriParams(String mapUriParams) {
        this.mapUriParams = mapUriParams;
    }
    /**
     * @param elements the elements to set
     */
    public void setElements(Set<Distribution> elements) {
        this.elements = elements;
    }

    /**
     * @return the condensedDistribution
     */
    public CondensedDistribution getCondensedDistribution() {
        return condensedDistribution;
    }
    /**
     * @return the tree
     */
    public DistributionTree getTree() {
        return tree;
    }
    /**
     * @return the mapUriParams
     */
    public String getMapUriParams() {
        return mapUriParams;
    }
    /**
     * @return the elements
     */
    public Set<Distribution> getElements() {
        return elements;
    }

    public enum InfoPart{
        condensedDistribution,
        tree,
        mapUriParams,
        elements,
    }


}
