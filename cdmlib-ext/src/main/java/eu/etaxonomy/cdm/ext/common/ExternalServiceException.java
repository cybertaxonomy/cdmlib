/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.common;

/**
 * @author a.kohlbecker
 * @since Feb 15, 2018
 *
 */
public class ExternalServiceException extends Exception {


    private static final long serialVersionUID = -7609872610682674127L;

    String externalService;

    /**
     * @param message
     * @param cause
     */
    public ExternalServiceException(String externalService, String message, Throwable cause) {
        super(message, cause);
        this.externalService = externalService;
    }

    /**
     * @param cause
     */
    public ExternalServiceException(String externalService, Throwable cause) {
        super(cause);
        this.externalService = externalService;
    }

    /**
     * @param preference
     * @param message
     */
    public ExternalServiceException(String externalService, String message) {
        super(message);
        this.externalService = externalService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return externalService + ": " + super.getMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalizedMessage() {
        return externalService + ": " + super.getLocalizedMessage();
    }





}
