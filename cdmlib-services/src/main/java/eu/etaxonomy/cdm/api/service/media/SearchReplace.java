/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

import java.util.regex.Pattern;

/**
 * Defines a search and replace operation for the
 * {@link MediaUriTransformation}. The search pattern is a regular expression.
 * Internally the search regex is compiled into a cached regex pattern when it
 * is requested by calling {@link #searchPattern}.
 * <p>
 * <b>CHANGING THIS CLASS MAY BREAK DESERIALIZATION OF EXISTING CDM PREFERENCES.</b>
 *
 * @author a.kohlbecker
 * @since Aug 19, 2020
 */
public class SearchReplace {

    private String replace;
    private String search;
    private Pattern searchPattern;

    public SearchReplace() {
    }

    /**
     * @param search
     *            the regular expressions to used as search pattern
     * @param replace
     *            The replacement string
     */
    public SearchReplace(String search, String replace) {
        this.search = search;
        this.replace = replace;
    }

    /**
     * Get the regular expressions used as search pattern
     */
    public String getSearch() {
        return search;
    }

    /**
     * @return The replacement string
     */
    public String getReplace() {
        return replace;
    }

    // not as property to avoid serialization
    public Pattern searchPattern() {
        if (searchPattern == null) {
            searchPattern = Pattern.compile(search);
        }
        return searchPattern;
    }

}