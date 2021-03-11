/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

/**
 * @author a.kohlbecker
 * @since Jun 23, 2017
 */
@SuppressWarnings("serial")
public class DerivedUnitConversionException extends Exception {

    public DerivedUnitConversionException(String message) {
        super(message);
    }

    public DerivedUnitConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}