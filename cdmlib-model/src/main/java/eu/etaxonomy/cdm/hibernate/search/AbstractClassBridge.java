/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * Base class for class bridges. The {@link AbstractClassBridge} basically provides a set of
 * {@link LuceneOptions} for id and for search fields.
 *
 * @author a.kohlbecker
 * @since Sep 24, 2012
 */
public abstract class AbstractClassBridge implements FieldBridge {

    protected final static NotNullAwareIdBridge idFieldBridge = new NotNullAwareIdBridge();

    /**
     * see {@link IdFieldOptions}
     */
    public static LuceneOptions idFieldOptions = new IdFieldOptions();

    @Override
    abstract public void set(String name, Object value, Document document, LuceneOptions luceneOptions);
}