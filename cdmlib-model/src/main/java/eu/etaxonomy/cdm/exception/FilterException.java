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
 * This Exception is for throwing an exception if a filter prevents a method
 * from returning any value except an empty one.
 * This is because the filter condition itself can never be <code>true</code>
 * (e.g. because a given subtree ID does not exist in the database at all)
 * or because non of the data matches the filter.
 * <BR>
 * In the first case it is recommended to set the invalidFilter parameter to <code>true</code>
 * to indicate that the filter itself might be not correct.
 *
 * @author a.mueller
 * @since 14.09.2018
 */
public class FilterException extends Exception {

    private static final long serialVersionUID = 7491596488082796101L;

    private boolean invalidFilter;

    public FilterException(boolean invalidFilter) {
        super();
        this.setInvalidFilter(invalidFilter);
    }

    public FilterException(String message, boolean invalidFilter) {
        super(message);
        this.setInvalidFilter(invalidFilter);
    }

    /**
     * @param cause
     */
    public FilterException(Throwable cause, boolean invalidFilter) {
        super(cause);
        this.setInvalidFilter(invalidFilter);
    }

    /**
     * @param message
     * @param cause
     */
    public FilterException(String message, Throwable cause, boolean invalidFilter) {
        super(message, cause);
        this.setInvalidFilter(invalidFilter);
    }

    public boolean isInvalidFilter() {
        return invalidFilter;
    }

    public void setInvalidFilter(boolean invalidFilter) {
        this.invalidFilter = invalidFilter;
    }

}
