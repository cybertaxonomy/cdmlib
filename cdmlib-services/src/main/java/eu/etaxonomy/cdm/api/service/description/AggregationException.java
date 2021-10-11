/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

/**
 * @author a.mueller
 * @since 07.10.2021
 */
public class AggregationException extends RuntimeException {

    private static final long serialVersionUID = -883213782380432255L;

    public AggregationException() {
        super();
    }

    public AggregationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AggregationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AggregationException(String message) {
        super(message);
    }

    public AggregationException(Throwable cause) {
        super(cause);
    }

}
