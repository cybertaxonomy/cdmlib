/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

/**
 * Interface that allows injecting a distribution tree (from service layer) to DistributionInfoDTO.
 * Might be a preliminary solution until it is solved in a better way.
 *
 * @author a.mueller
 * @date 09.02.2023
 */
public interface IDistributionTree {

//    void orderAsTree(Collection<Distribution> distributions, Set<NamedAreaLevel> omitLevels,
//            Set<MarkerType> fallbackAreaMarkerTypes, boolean neverUseFallbackAreaAsParent);
//
//    void recursiveSortChildren(DistributionOrder distributionOrder);
}
