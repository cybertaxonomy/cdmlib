/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.Set;

import eu.etaxonomy.cdm.common.Tree;

/**
 * @author a.mueller
 * @date 09.02.2023
 */
public class DistributionTreeDto
        extends Tree<Set<DistributionDto>, NamedAreaDto>
        implements IDistributionTree {

}