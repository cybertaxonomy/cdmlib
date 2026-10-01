/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service.search;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.hibernate.SessionFactory;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.backend.lucene.scope.LuceneIndexScope;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

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


    private SearchFactory getCurrentSearchFactory() {
        return org.hibernate.search.Search.getFullTextSession(sessionFactory.getCurrentSession()).getSearchFactory();
    }

    private SearchMapping getSearchMapping() {
        return Search.mapping(sessionFactory);
    }

    /**
     * Maps abstract indexed base types to a concrete subclass for APIs that still
     * require a directly {@code @Indexed} type (e.g. analyzer lookup via the v5 helper).
     * Index readers no longer need this: {@link SearchMapping#scope(Class)} includes
     * all indexed subtypes.
     */
    protected Class<? extends CdmBase> pushAbstractBaseTypeDown(Class<? extends CdmBase> type) {
        if(type == null) {
            throw new NullPointerException("parameter type must not be null");
        }
        if (type.equals(DescriptionElementBase.class)) {
            return TextData.class;
        }
        if (type.equals(TaxonBase.class)) {
            return Taxon.class;
        }
        if (type.equals(SpecimenOrObservationBase.class)) {
            return DerivedUnit.class;
        }
        return type;
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
     * <b>WARNING</b> For concrete {@code @Indexed} types this returns Hibernate Search's
     * per-field {@code IndexingScopedAnalyzer}. For abstract base types that span multiple
     * indexes in HS6 (e.g. {@link DescriptionElementBase}), that analyzer only knows the
     * fields of the pushed-down subtype (TextData) and would fail to tokenize fields such
     * as {@code name} or {@code area.label}. In that case a {@link StandardAnalyzer} is
     * used, which matches {@code AnalyzerNames.DEFAULT} for QueryParser purposes.
     */
    @Override
    public Analyzer getAnalyzerFor(Class<? extends CdmBase> clazz) {
        if (needsSharedQueryAnalyzer(clazz)) {
            return new StandardAnalyzer();
        }
        return getCurrentSearchFactory().getAnalyzer(pushAbstractBaseTypeDown(clazz));
    }

    private static boolean needsSharedQueryAnalyzer(Class<? extends CdmBase> clazz) {
        return clazz == null
                || Modifier.isAbstract(clazz.getModifiers())
                || clazz.equals(DescriptionElementBase.class)
                || clazz.equals(TaxonBase.class)
                || clazz.equals(SpecimenOrObservationBase.class);
    }

    @Override
    public QueryFactory newQueryFactoryFor(Class<? extends CdmBase> clazz){
        // Keep the original clazz for analyzer selection; do not push abstract types down
        // here or QueryFactory would analyse DescriptionElement queries with TextData's
        // IndexingScopedAnalyzer (missing name / area.label / …).
        return new QueryFactory(this, clazz);
    }

}
