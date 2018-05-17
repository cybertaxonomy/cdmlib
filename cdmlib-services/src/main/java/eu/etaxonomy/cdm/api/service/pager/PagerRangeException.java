/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.pager;

/**
 * @author a.mueller
 * @since 22.09.2016
 *
 */
public class PagerRangeException extends RuntimeException {

    private static final long serialVersionUID = 1626299671702704921L;

    /**
     * @param message
     */
    public PagerRangeException(String message) {
        super(message);
    }




}
