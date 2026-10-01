/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service.search;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.hibernate.SessionFactory;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.backend.lucene.scope.LuceneIndexScope;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since Sep 18, 2013
 */
@Component
public class LuceneIndexToolProviderImpl implements ILuceneIndexToolProvider {

    private final static String DEFAULT_QURERY_FIELD_NAME = "titleCache";

    @Autowired
    private SessionFactory sessionFactory;

    private final Map<Class<? extends CdmBase>, QueryParser> queryParsers = new HashMap<>();
    private final Map<Class<? extends CdmBase>, QueryParser> complexPhraseQueryParsers = new HashMap<>();

    private SearchMapping getSearchMapping() {
        return Search.mapping(sessionFactory);
    }

    @Override
    public IndexReader getIndexReaderFor(Class<? extends CdmBase> clazz) {
        // Scope on a parent type covers all indexed subtypes (separate indexes in HS6).
        LuceneIndexScope scope = getSearchMapping()
                .scope(clazz)
                .extension(LuceneExtension.get());
        return scope.openIndexReader();
    }

    @Override
    public QueryParser getQueryParserFor(Class<? extends CdmBase> clazz, boolean complexPhraseQuery) {
        if(!complexPhraseQuery){
            if(!queryParsers.containsKey(clazz)){
                Analyzer analyzer = getAnalyzerFor(clazz);
                QueryParser parser = new QueryParser(DEFAULT_QURERY_FIELD_NAME, analyzer);
                parser.setAllowLeadingWildcard(true);
                queryParsers.put(clazz, parser);
            }
            return queryParsers.get(clazz);
        } else {
            if(!complexPhraseQueryParsers.containsKey(clazz)){
                Analyzer analyzer = getAnalyzerFor(clazz);
                QueryParser parser = new ComplexPhraseQueryParser(DEFAULT_QURERY_FIELD_NAME, analyzer);
                parser.setAllowLeadingWildcard(true);
                complexPhraseQueryParsers.put(clazz, parser);
            }
            return complexPhraseQueryParsers.get(clazz);
        }
    }


    /**
     * Returns a {@link StandardAnalyzer}, which matches {@code AnalyzerNames.DEFAULT}
     * used by most CDM index fields. Per-type {@code IndexingScopedAnalyzer} from HS5
     * is no longer available via a simple mapper API in HS6.
     */
    @Override
    public Analyzer getAnalyzerFor(Class<? extends CdmBase> clazz) {
        return new StandardAnalyzer();
    }

    @Override
    public QueryFactory newQueryFactoryFor(Class<? extends CdmBase> clazz){
        return new QueryFactory(this, clazz);
    }

}
