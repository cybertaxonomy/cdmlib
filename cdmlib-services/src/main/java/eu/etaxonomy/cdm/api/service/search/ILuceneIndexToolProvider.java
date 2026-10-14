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

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since Sep 18, 2013
 */
public interface ILuceneIndexToolProvider {

    /**
     * Opens an {@link IndexReader} for the lucene index of the given
     * <code>clazz</code>. Callers must {@link IndexReader#close() close} the reader.
     */
    public abstract IndexReader getIndexReaderFor(Class<? extends CdmBase> clazz);

    /**
     * Either creates a new QueryParser or returns the QueryParser which has
     * been created before for the specified class. The QueryParsers per CdmBase
     * type are cached in a Map.
     * @param complexPhraseQuery TODO
     *
     * @return the QueryParser suitable for the lucene index of the given
     *         <code>clazz</code>
     */
    public abstract QueryParser getQueryParserFor(Class<? extends CdmBase> clazz, boolean complexPhraseQuery);

    /**
     * Returns an Analyzer suitable for parsing queries against the given type's
     * Lucene index. Implementations typically use {@link org.apache.lucene.analysis.standard.StandardAnalyzer},
     * matching Hibernate Search's default analyzer.
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

}
