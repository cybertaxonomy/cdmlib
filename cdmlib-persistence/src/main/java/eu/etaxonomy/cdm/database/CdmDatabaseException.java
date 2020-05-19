/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import eu.etaxonomy.cdm.config.CdmSourceException;

/**
 * RuntimeException which is thrown in case of severe problems with the configured
 * CDM database which prevent the application from starting up. Other less severe problems
 * should be reported using {@link CdmSourceException}.
 *
 * @author a.kohlbecker
 * @date May 18, 2020
 *
 */
public class CdmDatabaseException extends RuntimeException {


    public CdmDatabaseException(String string) {
        super(string);
    }

    public CdmDatabaseException(String string, Throwable t) {
        super(string, t);
    }


    private static final long serialVersionUID = 1L;

}
