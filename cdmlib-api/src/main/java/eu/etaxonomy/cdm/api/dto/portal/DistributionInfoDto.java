/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.Set;

import eu.etaxonomy.cdm.format.description.distribution.CondensedDistribution;
import eu.etaxonomy.cdm.model.description.Distribution;

/**
 * @author a.kohlbecker
 * @since Jan 29, 2014
 */
public class DistributionInfoDto implements IFactDto {

    private CondensedDistribution condensedDistribution = null;
    private IDistributionTree tree = null;
    private String mapUriParams = null;

    //TODO remove elements ??
    private Set<Distribution> elements = null;

// ****************** GETTER / SETTER ******************************/

    public CondensedDistribution getCondensedDistribution() {
        return condensedDistribution;
    }
    public void setCondensedDistribution(CondensedDistribution condensedDistribution) {
        this.condensedDistribution = condensedDistribution;
    }

    public IDistributionTree getTree() {
        return tree;
    }
    public void setTree(IDistributionTree tree) {
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