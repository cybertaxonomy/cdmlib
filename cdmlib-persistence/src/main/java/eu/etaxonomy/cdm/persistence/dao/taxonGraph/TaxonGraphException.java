/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.taxonGraph;

/**
 * @author a.kohlbecker
 * @since Sep 26, 2018
 *
 */
public class TaxonGraphException extends Exception {

    private static final long serialVersionUID = 1744987554049545434L;

    /**
     * @param message
     * @param cause
     */
    public TaxonGraphException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public TaxonGraphException(String message) {
        super(message);
    }


}
