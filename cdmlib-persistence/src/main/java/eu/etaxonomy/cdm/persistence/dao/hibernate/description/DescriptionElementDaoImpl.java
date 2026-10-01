/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.hibernate.Hibernate;
import org.hibernate.search.mapper.orm.Search;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoBaseImpl;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class DescriptionElementDaoImpl
        extends AnnotatableDaoBaseImpl<DescriptionElementBase>
        implements IDescriptionElementDao {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private final String defaultField = "multilanguageText.text";

    private final Class<? extends DescriptionElementBase> indexedClasses[];

    public DescriptionElementDaoImpl() {
        super(DescriptionElementBase.class);
        indexedClasses = new Class[1];
        indexedClasses[0] = TextData.class;
    }

    @Override
    public long count(Class<? extends DescriptionElementBase> clazz, String queryString) {
        checkNotInPriorView("DescriptionElementDaoImpl.countTextData(String queryString)");
        QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());

        try {
            org.apache.lucene.search.Query query = queryParser.parse(queryString);
            @SuppressWarnings("unchecked")
            Class<DescriptionElementBase> entityType = (Class<DescriptionElementBase>) (clazz == null ? type : clazz);
            return countFullTextResults(entityType, query);
        } catch (ParseException e) {
            throw new QueryParseException(e, queryString);
        }
    }

    @Override
    public List<DescriptionElementBase> search(Class<? extends DescriptionElementBase> clazz, String queryString, Integer pageSize,	Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
//    public <S extends DescriptionElementBase> List<S> search(Class<S> clazz, String queryString, Integer pageSize,  Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        checkNotInPriorView("DescriptionElementDaoImpl.searchTextData(String queryString, Integer pageSize,	Integer pageNumber)");
        QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());

        try {
            org.apache.lucene.search.Query query = queryParser.parse(queryString);
            @SuppressWarnings("unchecked")
            Class<DescriptionElementBase> entityType = (Class<DescriptionElementBase>) (clazz == null ? type : clazz);
            List<DescriptionElementBase> results = executeFullTextSearch(entityType, query, orderHints, pageSize, pageNumber);
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        } catch (ParseException e) {
            throw new QueryParseException(e, queryString);
        }
    }

    @Override
    public void purgeIndex() {
        var searchSession = Search.session(getSession());
        for(Class<? extends DescriptionElementBase> clazz : indexedClasses) {
            searchSession.workspace(clazz).purge();
        }
    }

    @Override
    public void rebuildIndex() {
        var indexingPlan = Search.session(getSession()).indexingPlan();

        for(DescriptionElementBase descriptionElementBase : list(null,null)) { // re-index all descriptionElements
            Hibernate.initialize(descriptionElementBase.getInDescription());
            Hibernate.initialize(descriptionElementBase.getFeature());
            indexingPlan.addOrUpdate(descriptionElementBase);
        }
        indexingPlan.execute();
    }

    @Override
    public void optimizeIndex() {
        var searchSession = Search.session(getSession());
        for(Class<? extends DescriptionElementBase> clazz : indexedClasses) {
            searchSession.workspace(clazz).mergeSegments();
        }
    }

    public int count(String queryString) {
        checkNotInPriorView("DescriptionElementDaoImpl.count(String queryString)");
        QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());

        try {
            org.apache.lucene.search.Query query = queryParser.parse(queryString);
            return Math.toIntExact(countFullTextResults(type, query));
        } catch (ParseException e) {
            throw new QueryParseException(e, queryString);
        }
    }

    @Override
    public String suggestQuery(String string) {
        throw new UnsupportedOperationException("suggest query is not supported yet");
    }

}
