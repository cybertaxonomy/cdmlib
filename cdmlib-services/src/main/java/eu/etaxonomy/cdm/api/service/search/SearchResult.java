/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * TODO class description
 *
 * @author Andreas Kohlbecker
 * @since Jan 6, 2012
 *
 */
public class SearchResult<T extends CdmBase> extends DocumentSearchResult {

    private T entity;

    public SearchResult() {}

    public T getEntity() {
        return entity;
    }
    public void setEntity(T entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "SearchResult [entity=" + entity + "]";
    }

}
