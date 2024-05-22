/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.exception;

/**
 * @author andreabee90
 * @since 21.05.2024
 */
public class NameMatchingParserException extends DataChangeNoRollbackException {

    String warning;

    public NameMatchingParserException (String warning) {
        super(warning);
        this.warning = warning;
    }

    public String getWarning() {
        return warning;
    }
    public void setWarning(String warning) {
        this.warning = warning;
    }
}
