/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.pager;

import java.util.List;

/**
 * Abstract class that represents a single page in a set of objects
 * returned from a query (possibly a subset of the total number of matching objects
 * available).
 *
 * NOTE: Indices for objects and pages are 0-based.
 * @author ben
 *
 * @param <T>
 */
public interface Pager<T> {
    /**
     * The total number of pages available for this query, or 0 if there are
     * no matching objects
     *
     * @return The number of pages available
     */
    public Integer getPagesAvailable();

    /**
     * The index of the next page in this result set, or null if this is the
     * last page in the result set.
     * @return The index of the next page
     */
    public Integer getNextIndex();

    /**
     * The index of the previous page in this result set, or null if this is the
     * first page in the result set.
     * @return The index of the previous page
     */
    public Integer getPrevIndex();

    /**
     * The index of this page.
     *
     * NOTE: Indices for objects and pages are 0-based.
     * @return The index of this page
     */
    public Integer getCurrentIndex();

    /**
     * Get a string label for a given page
     * (NOTE: Labels may not be calculated for each page in the result set,
     *  especially if the result set is large or the operation for calculating the
     *  label is expensive. The indices of the pages for which labels are available
     *  are given by {@link #getIndices()}.
     *
     * @param index
     * @return A label for the page indicated or null if this pager has not calculated a label for that page
     */
    public String getPageNumber(int index);

    /**
     * Gets the size of pages in this result set. Can be null if all matching
     * objects should be returned.
     * @return The page size
     */
    public Integer getPageSize();

    /**
     * Get a list of page indices for which labels are available.
     * @return A list of indices
     */
    public List<Integer> getIndices();

    /**
     * Get the total number of objects in this result set (not in this page).
     * If count > {@link #getPageSize()} then {@link #getPagesAvailable()} > 1
     * @return the total number of objects available.
     */
    public Long getCount();

    /**
     * Returns the index of the first record in this result set
     * @return
     */
    public Integer getFirstRecord();

    /**
     * Returns the index of the last record in this result set
     * @return
     */
    public Integer getLastRecord();

    /**
     * Returns the records in this page.
     * @return
     */
    public List<T> getRecords();

    /**
     * Returns a suggested query string (only applicable for free-text / lucene queries).
     *
     * Usually only calculated if there were no matching results for the original query.
     * @return
     */
    public String getSuggestion();

}
