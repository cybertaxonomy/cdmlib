package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class DescriptionElementDaoImpl extends AnnotatableDaoImpl<DescriptionElementBase> implements IDescriptionElementDao {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DescriptionElementDaoImpl.class);

    private final String defaultField = "multilanguageText.text";

    private final Class<? extends DescriptionElementBase> indexedClasses[];

    public DescriptionElementDaoImpl() {
        super(DescriptionElementBase.class);
        indexedClasses = new Class[1];
        indexedClasses[0] = TextData.class;
    }

    @Override
    public int countMedia(DescriptionElementBase descriptionElement) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query query = getSession().createQuery("select count(media) from DescriptionElementBase descriptionElement join descriptionElement.media media where descriptionElement = :descriptionElement");
            query.setParameter("descriptionElement", descriptionElement);

            return ((Long)query.uniqueResult()).intValue();
        } else {
            // Horribly inefficient, I know, but hard to do at the moment with envers.
            // FIXME Improve this (by improving envers)
            List<String> propertyPaths = new ArrayList<String>();
            propertyPaths.add("media");
            DescriptionElementBase d = super.load(descriptionElement.getUuid(), propertyPaths);
            return d.getMedia().size();
        }
    }

    @Override
    public int count(Class<? extends DescriptionElementBase> clazz, String queryString) {
        checkNotInPriorView("DescriptionElementDaoImpl.countTextData(String queryString)");
        QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());

        try {
            org.apache.lucene.search.Query query = queryParser.parse(queryString);

            FullTextSession fullTextSession = Search.getFullTextSession(getSession());
            org.hibernate.search.FullTextQuery fullTextQuery = null;

            if(clazz == null) {
                fullTextQuery = fullTextSession.createFullTextQuery(query, type);
            } else {
                fullTextQuery = fullTextSession.createFullTextQuery(query, clazz);
            }
            return  fullTextQuery.getResultSize();
        } catch (ParseException e) {
            throw new QueryParseException(e, queryString);
        }
    }

    @Override
    public List<Media> getMedia(DescriptionElementBase descriptionElement, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query query = getSession().createQuery("select media from DescriptionElementBase descriptionElement join descriptionElement.media media where descriptionElement = :descriptionElement order by index(media)");
            query.setParameter("descriptionElement", descriptionElement);

            addPageSizeAndNumber(query, pageSize, pageNumber);

            @SuppressWarnings("unchecked")
            List<Media> results = query.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        } else {
            // Horribly inefficient, I know, but hard to do at the moment with envers.
            // FIXME Improve this (by improving envers)
            List<String> pPaths = new ArrayList<>();
            propertyPaths.add("media");
            DescriptionElementBase d = super.load(descriptionElement.getUuid(), pPaths);
            List<Media> results = new ArrayList<>();
            results.addAll(d.getMedia());
            if(pageSize != null) {
                int fromIndex = 0;
                int toIndex = 0;
                if(pageNumber != null) {
                    // if the page is out of scope
                    if(results.size() < (pageNumber * pageSize)) {
                        return new ArrayList<>();
                    }
                    fromIndex =   pageNumber * pageSize;
                }
                toIndex = results.size() < (fromIndex + pageSize) ? results.size() : fromIndex + pageSize;
                results = results.subList(fromIndex, toIndex);
            }
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
    }

    @Override
    public List<DescriptionElementBase> search(Class<? extends DescriptionElementBase> clazz, String queryString, Integer pageSize,	Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        checkNotInPriorView("DescriptionElementDaoImpl.searchTextData(String queryString, Integer pageSize,	Integer pageNumber)");
        QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());

        try {
            org.apache.lucene.search.Query query = queryParser.parse(queryString);
            org.hibernate.search.FullTextQuery fullTextQuery = null;
            FullTextSession fullTextSession = Search.getFullTextSession(getSession());
            if(clazz == null) {
                fullTextQuery = fullTextSession.createFullTextQuery(query, type);
            } else {
                fullTextQuery = fullTextSession.createFullTextQuery(query, clazz);
            }
            addOrder(fullTextQuery,orderHints);

            addPageSizeAndNumber(fullTextQuery, pageSize, pageNumber);

            @SuppressWarnings("unchecked")
            List<DescriptionElementBase> results = fullTextQuery.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;

        } catch (ParseException e) {
            throw new QueryParseException(e, queryString);
        }
    }

    @Override
    public void purgeIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        for(Class<? extends DescriptionElementBase> clazz : indexedClasses) {
            fullTextSession.purgeAll(type); // remove all description element base from indexes
        }
        fullTextSession.flushToIndexes();
    }

    @Override
    public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        for(DescriptionElementBase descriptionElementBase : list(null,null)) { // re-index all descriptionElements
            Hibernate.initialize(descriptionElementBase.getInDescription());
            Hibernate.initialize(descriptionElementBase.getFeature());
            fullTextSession.index(descriptionElementBase);
        }
        fullTextSession.flushToIndexes();
    }

    @Override
    public void optimizeIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        SearchFactory searchFactory = fullTextSession.getSearchFactory();
        for(Class<? extends DescriptionElementBase> clazz : indexedClasses) {
            searchFactory.optimize(clazz); // optimize the indices ()
        }
        fullTextSession.flushToIndexes();
    }

    public int count(String queryString) {
        checkNotInPriorView("DescriptionElementDaoImpl.count(String queryString)");
        QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());

        try {
            org.apache.lucene.search.Query query = queryParser.parse(queryString);

            FullTextSession fullTextSession = Search.getFullTextSession(this.getSession());
            org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, type);

            return fullTextQuery.getResultSize();

        } catch (ParseException e) {
            throw new QueryParseException(e, queryString);
        }
    }

    @Override
    public String suggestQuery(String string) {
        throw new UnsupportedOperationException("suggest query is not supported yet");
    }

}
