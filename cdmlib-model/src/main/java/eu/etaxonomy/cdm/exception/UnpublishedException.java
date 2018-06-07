/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.exception;

/**
 * An Exception shown if data is unpublished and therefore under the given
 * circumstances not readable.
 *
 * @author a.mueller
 * @since 07.06.2018
 */
public class UnpublishedException extends Exception {

    private static final long serialVersionUID = 1901079330887825007L;

    /**
     *
     */
    public UnpublishedException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public UnpublishedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public UnpublishedException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public UnpublishedException(Throwable cause) {
        super(cause);
    }

}
