// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.pager;

/**
 * @see also class <code>eu.etaxonomy.cdm.remote.controller.util.PagerParameter</code>
 *
 * @author a.kohlbecker
 * @date Dec 10, 2012
 *
 */
public class PagerUtils {

    public static Integer startFor(Integer pageSize, Integer pageIndex) {
        if(pageIndex == null) {
            pageIndex = 0;
        }
        return pageSize *  pageIndex;

    }

    public static Integer limitFor(Integer pageSize) {
        if(pageSize == null){
            return null;
        }
        return pageSize;
    }

}
