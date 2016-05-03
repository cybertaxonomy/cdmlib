// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.pager.impl;

import java.util.List;

public class DefaultPagerImpl<T> extends AbstractPagerImpl<T> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param currentIndex the page of this result set (0-based), can be null
     * @param count the total number of results available for this query
     * @param pageSize The size of pages (can be null or 0 if all results should be returned if available)
     * @param records A list of objects in this page (can be empty if there were no results)
     *
     * @deprecated This constructor only supports total result counts to {@link Integer#MAX_VALUE} u
     * use {@link DefaultPagerImpl(Integer currentIndex, Long count, Integer pageSize, List<T> records, String suggestion)}
     * instead
     */
    @Deprecated
    public DefaultPagerImpl(Integer currentIndex, Integer count, Integer pageSize,	List<T> records) {
        super(currentIndex, count, pageSize, records);
    }

    /**
     * Constructor
     *
     * @param currentIndex the page of this result set (0-based), can be null
     * @param count the total number of results available for this query
     * @param pageSize The size of pages (can be null or 0 if all results should be returned if available)
     * @param records A list of objects in this page (can be empty if there were no results)
     */
    public DefaultPagerImpl(Integer currentIndex, Long count, Integer pageSize,  List<T> records) {
        super(currentIndex, count, pageSize, records);
    }

    /**
     * Constructor
     *
     * @param currentIndex the page of this result set (0-based), can be null
     * @param count the total number of results available for this query
     * @param pageSize The size of pages (can be null if all results should be returned if available)
     * @param records A list of objects in this page (can be empty if there were no results)
     * @param suggestion a suggested query that would improve the search (only applicable for free-text / lucene queries)
     *
     * @deprecated This constructor only supports total result counts to {@link Integer#MAX_VALUE} u
     * use {@link DefaultPagerImpl(Integer currentIndex, Integer count, Integer pageSize, List<T> records, String suggestion)}
     * instead
     */
    @Deprecated
    public DefaultPagerImpl(Integer currentIndex, Integer count, Integer pageSize,	List<T> records, String suggestion) {
        super(currentIndex, count, pageSize, records,suggestion);
    }

    @Override
    protected String createLabel(String s1, String s2) {
        return s1 + DefaultPagerImpl.LABEL_DIVIDER + s2;
    }
}
