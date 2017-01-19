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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.hibernate.SessionFactory;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.indexes.IndexReaderAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.kohlbecker
 * @date Sep 18, 2013
 *
 */
@Component
public class LuceneIndexToolProviderImpl implements ILuceneIndexToolProvider {

    private final static String DEFAULT_QURERY_FIELD_NAME = "titleCache";

    @Autowired
    private SessionFactory sessionFactory;

    private final Map<Class<? extends CdmBase>, QueryParser> queryParsers = new HashMap<Class<? extends CdmBase>, QueryParser>();
    private final Map<Class<? extends CdmBase>, QueryParser> complexPhraseQueryParsers = new HashMap<Class<? extends CdmBase>, QueryParser>();

    /**
     * @param sessionfactory
     * @return
     */
    private SearchFactory getCurrentSearchFactory() {
        return Search.getFullTextSession(sessionFactory.getCurrentSession()).getSearchFactory();
    }


    /**
     * TODO the abstract base class DescriptionElementBase can not be used, so
     * we are using an arbitrary subclass to find the DirectoryProvider, future
     * versions of hibernate search my allow using abstract base classes see
     * {@link http://stackoverflow.com/questions/492184/how-do-you-find-all-subclasses-of-a-given-class-in-java}
     *
     * @param type must not be null
     * @return
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
        if (type.equals(TaxonNameBase.class)) {
            return NonViralName.class;
        }
        return type;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider#getIndexReaderFor(java.lang.Class)
     */
    @Override
    public IndexReader getIndexReaderFor(Class<? extends CdmBase> clazz) {
        IndexReader reader = getCurrentSearchFactory().getIndexReaderAccessor().open(pushAbstractBaseTypeDown(clazz));
        return reader;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider#getQueryParserFor(java.lang.Class, boolean complexPhraseQuery)
     */
    @Override
    public QueryParser getQueryParserFor(Class<? extends CdmBase> clazz, boolean complexPhraseQuery) {
        if(!complexPhraseQuery){
            if(!queryParsers.containsKey(clazz)){
                Analyzer analyzer = getAnalyzerFor(clazz);
                QueryParser parser = new QueryParser(DEFAULT_QURERY_FIELD_NAME, analyzer);
                queryParsers.put(clazz, parser);
            }
            return queryParsers.get(clazz);
        } else {
            if(!complexPhraseQueryParsers.containsKey(clazz)){
                Analyzer analyzer = getAnalyzerFor(clazz);
                QueryParser parser = new ComplexPhraseQueryParser(DEFAULT_QURERY_FIELD_NAME, analyzer);
                complexPhraseQueryParsers.put(clazz, parser);
            }
            return complexPhraseQueryParsers.get(clazz);
        }
    }


    /**
     * <b>WARING</b> This method might return an Analyzer
     * which is not suitable for all fields of the lucene document. This method
     * internally uses the simplified method from {@link {
     * @link org.hibernate.search.SearchFactory#getAnalyzer(Class)}
     *
     * TODO implement method which allows to retrieve the correct Analyzer
     * per document field, this method will have another signature.
     *
     * @return the Analyzer suitable for the lucene index of the given
     *         <code>clazz</code>
     */
    @Override
    public Analyzer getAnalyzerFor(Class<? extends CdmBase> clazz) {
        Analyzer analyzer = getCurrentSearchFactory().getAnalyzer(pushAbstractBaseTypeDown(clazz));
        return analyzer;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider#getQueryFactoryFor(java.lang.Class)
     */
    @Override
    public QueryFactory newQueryFactoryFor(Class<? extends CdmBase> clazz){
        return new QueryFactory(this, pushAbstractBaseTypeDown(clazz));
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider#getIndexReaderAccessor()
     */
    @Override
    public IndexReaderAccessor getIndexReaderAccessor(){
        return getCurrentSearchFactory().getIndexReaderAccessor();
    }

}
