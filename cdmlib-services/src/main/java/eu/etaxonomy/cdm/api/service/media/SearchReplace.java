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

    public SearchReplace(String search, String replace) {
        this.search = search;
        this.replace = replace;
    }

    public String getSearch() {
        return search;
    }

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