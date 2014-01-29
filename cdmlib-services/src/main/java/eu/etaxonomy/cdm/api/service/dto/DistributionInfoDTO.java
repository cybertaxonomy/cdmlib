// $Id$
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
 * @date Jan 29, 2014
 *
 */
public class DistributionInfoDTO {

    private final String condensedStatusString = null;
    private final DistributionTree tree = null;
    private final String mapUriParams = null;
    private final Set<Distribution> elements = null;
    /**
     * @return the condensedStatusString
     */
    public String getCondensedStatusString() {
        return condensedStatusString;
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
        condensedStatusString,
        tree,
        mapUriParams,
        elements,
    }


}
