/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

/**
 * Lucene document field names used by the CDM free-text search layer.
 * Replaces Hibernate Search 5 {@code ProjectionConstants} values that no longer exist in HS6.
 */
public final class LuceneIndexFields {

    private LuceneIndexFields() {}

    /**
     * Fully qualified entity class name. Written by {@link eu.etaxonomy.cdm.hibernate.search.ClassInfoBridge}
     * (HS5 used {@code ProjectionConstants.OBJECT_CLASS} / {@code _hibernate_class}).
     */
    public static final String OBJECT_CLASS = "classInfo";
}
