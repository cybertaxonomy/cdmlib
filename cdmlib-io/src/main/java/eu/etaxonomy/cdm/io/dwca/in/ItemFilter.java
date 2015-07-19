// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;


/**
 * Interface which allows filtering of stream items.
 * @author a.mueller
 * @date 19.07.2015
 *
 */
public interface ItemFilter<ITEM extends Object> {

    /**
     * The implementation of this method must return true for
     * all items which will be used during import (not filtered out).
     * False for all items to filter out (not be used during this import).
     * @param item
     * @return
     */
    public boolean toBeUsed(ITEM item);
}
