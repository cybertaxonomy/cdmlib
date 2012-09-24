// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * Global default options for ID fields:
 * <ul>
 * <li>Store.YES</li>
 * <li>Index.NOT_ANALYZED</li>
 * <li>TermVector.NO</li>
 * <li>Boost = 1.0f (neutral default boost)</li>
 *
 * @author andreas
 * @date Sep 24, 2012
 *
 */
public class SortFieldOptions implements LuceneOptions {


    @Override
    public Store getStore() {
        return Store.YES;
    }

    @Override
    public Index getIndex() {
        return Index.NOT_ANALYZED;
    }

    @Override
    public TermVector getTermVector() {
        return TermVector.NO;
    }

    @Override
    public Float getBoost() {
        return 1.0f;
    }

}
