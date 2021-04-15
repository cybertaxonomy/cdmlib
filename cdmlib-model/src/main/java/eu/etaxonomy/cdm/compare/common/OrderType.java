/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.common;

/**
 * @author a.mueller
 * @since 12.03.2021
 */
public enum OrderType {
    ALPHABETIC,
    NATURAL,  //for objects ordered in lists the list order, for objects ordered in trees the depth-first tree-order
    NATURAL_BREADTH_FIRST, //like NATURAL, but for objects ordered in trees ordered the breadth-first tree-order
}
