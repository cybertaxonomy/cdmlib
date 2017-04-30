/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author a.mueller
 * @date 24.03.2017
 *
 */
public abstract class IoResultBase {

    private List<byte[]> errors = new ArrayList<>();
    private List<byte[]> warnings = new ArrayList<>();
    private List<Exception> exceptions = new ArrayList<>();

// ************* GETTERS / SETTERS / ADDERS ***********************/

    public List<byte[]> getErrors() {return errors;}
    public void setErrors(List<byte[]> errors) {this.errors = errors;}
    public void addError(String error) {
        errors.add(error.getBytes(StandardCharsets.UTF_8));
    }

    public List<byte[]> getWarnings() {return warnings;}
    public void setWarnings(List<byte[]> warnings) {this.warnings = warnings;}
    public void addWarning(String warning) {
        warnings.add(warning.getBytes(StandardCharsets.UTF_8));
    }

    public List<Exception> getExceptions() {return exceptions;}
    public void setExceptions(List<Exception> exceptions) {this.exceptions = exceptions;}
    public void addException(Exception e) {
        exceptions.add(e);
        setExceptionState();
    }

    protected abstract void setExceptionState();

    /**
     * Adds an error and aborts the import.
     * @param string
     */
    public void setAborted(String error) {
        this.addError(error);
        this.setAborted();
    }

    public abstract void setAborted();
}
