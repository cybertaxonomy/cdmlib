/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.time.LocalDateTime;
import java.util.Set;

import eu.etaxonomy.cdm.format.description.distribution.CondensedDistribution;
import eu.etaxonomy.cdm.model.description.Distribution;

/**
 * DTO which holds all information required by the dataportal to display
 * distributions.<BR>
 * These are the<ol>
 *  <li>map params to show the distribution on a map</li>
 *  <li>the distribution tree for hierarchical text representation</li>
 *  <li>the condensed distribution info for an abbreviated 1-line representation</li>
 * </ol>
 *
 * @author a.kohlbecker
 * @since Jan 29, 2014
 */
public class DistributionInfoDto implements IFactDto {

    private CondensedDistribution condensedDistribution = null;
    private IDistributionTree tree = null;
    private String mapUriParams = null;
    private LocalDateTime lastUpdated;

    //TODO remove elements ??
    private Set<Distribution> elements = null;

// ****************** GETTER / SETTER ******************************/

    @Override
    public String getClazz() {
        return this.getClass().getSimpleName();
    }

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
    }
    public void setElements(Set<Distribution> elements) {
        this.elements = elements;
    }

    @Override
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    @Override
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public enum InfoPart{
        condensedDistribution,  //include condensed distribution string
        tree,  //include area tree holding distribution information
        mapUriParams, //include uri params to show map
        elements,  //include model objects
    }
}