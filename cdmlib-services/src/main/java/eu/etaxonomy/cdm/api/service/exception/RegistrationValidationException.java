/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * @author a.kohlbecker
 * @since Mar 23, 2017
 *
 */
@SuppressWarnings("serial")
public class RegistrationValidationException extends Exception {

    List<String> problems = new ArrayList<>();

    /**
     * @param message
     */
    public RegistrationValidationException(String message, List<String> problems) {
        super(message);
        this.problems = problems;
    }

    @Override
    public String getMessage() {
        StringBuffer sb = new StringBuffer(super.getMessage()).append(" - Problems:");
        problems.forEach(p -> sb.append("- ").append(p).append("\n"));
        return sb.toString();
    }

    public List<String> getProblems() {
        return problems;
    }


}
