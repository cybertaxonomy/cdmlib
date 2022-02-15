/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.opt.config;

/**
 * @author a.kohlbecker
 * @since May 3, 2021
 */
public class DataSourceException extends RuntimeException {

    private static final long serialVersionUID = 977162383114750817L;


    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}