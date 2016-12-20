/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.hibernate.search.indexes.IndexReaderAccessor;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @date Sep 18, 2013
 *
 */
public interface ILuceneIndexToolProvider {

    /**
     * @return the IndexReader suitable for the lucene index of the given
     *         <code>clazz</code>
     */
    public abstract IndexReader getIndexReaderFor(Class<? extends CdmBase> clazz);

    /**
     * Either creates a new QueryParser or returns the QueryParser which has
     * been created before for the specified class. The QueryParsers per CdmBase
     * type are cached in a Map.
     *
     * @return the QueryParser suitable for the lucene index of the given
     *         <code>clazz</code>
     */
    public abstract QueryParser getQueryParserFor(Class<? extends CdmBase> clazz);

    /**
     * <b>WARING</b> The implementation of this method might return an Analyzer
     * which is not suitable for all fields of the lucene document. This method
     * internally uses the simplified method from {@link {
     * @link org.hibernate.search.SearchFactory#getAnalyzer(Class)}
     *
     * @return the Analyzer suitable for the lucene index of the given
     *         <code>clazz</code>
     */
    public abstract Analyzer getAnalyzerFor(Class<? extends CdmBase> clazz);

    /**
     * Creates new QueryFactory for the specified Cdm type.
     *
     * @return A new QueryFactory suitable for the lucene index of the given
     *         <code>clazz</code>
     */
    public abstract QueryFactory newQueryFactoryFor(Class<? extends CdmBase> clazz);

    /**
     * @return the IndexReaderAccessor from the SearchFactory
     */
    public abstract IndexReaderAccessor getIndexReaderAccessor();

}
