/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.idminter;

/**
 * Thrown by a minter in case there to no identifier left in range available to the minter.
 *
 * @author a.kohlbecker
 * @since Dec 12, 2017
 *
 */
public class OutOfIdentifiersException extends RuntimeException {

    /**
     * @param string
     */
    public OutOfIdentifiersException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 4613813239079488678L;

}
