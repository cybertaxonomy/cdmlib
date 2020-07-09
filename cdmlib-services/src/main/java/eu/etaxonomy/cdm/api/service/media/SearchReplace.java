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

public class SearchReplace {

    private String replace;
    private String search;
    private Pattern searchPattern;


    public SearchReplace() {

    }

    /**
     * @param search
     * @param replace
     */
    public SearchReplace(String search, String replace) {
        this.search = search;
        this.replace = replace;
    }

    /**
     * @return the search
     */
    public String getSearch() {
        return search;
    }

    /**
     * @return the replace
     */
    public String getReplace() {
        return replace;
    }

    /**
     * @return the search
     */
    public Pattern getSearchPattern() {
        if (searchPattern == null) {
            searchPattern = Pattern.compile(search);
        }
        return searchPattern;
    }

}