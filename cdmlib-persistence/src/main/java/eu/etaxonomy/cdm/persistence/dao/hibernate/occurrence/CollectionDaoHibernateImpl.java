/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.occurrence.ICollectionDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class CollectionDaoHibernateImpl extends IdentifiableDaoBase<Collection> implements
		ICollectionDao {
	
	private static final String defaultField = "titleCache";
		
	public CollectionDaoHibernateImpl() {
		super(Collection.class);
	}

	public List<Collection> getCollectionByCode(String code) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria crit = getSession().createCriteria(Collection.class);
    		crit.add(Restrictions.eq("code", code));
		
		    return (List<Collection>)crit.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Collection.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("code").eq(code));
			return (List<Collection>)query.getResultList();
		}
	}

	public int count(Class<? extends Collection> clazz, String queryString) {
		checkNotInPriorView("CollectionDaoHibernateImpl.count(Class<? extends Collection> clazz, String queryString)");
        QueryParser queryParser = new QueryParser(defaultField, new SimpleAnalyzer());
		
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.getFullTextSession(this.getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = null;
			
			if(clazz == null) {
				fullTextQuery = fullTextSession.createFullTextQuery(query, type);
			} else {
				fullTextQuery = fullTextSession.createFullTextQuery(query, clazz);
			}
			
		    Integer  result = fullTextQuery.getResultSize();
		    return result;

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}

	public void optimizeIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		SearchFactory searchFactory = fullTextSession.getSearchFactory();
	    searchFactory.optimize(Collection.class); // optimize the indices ()
	    fullTextSession.flushToIndexes();
	}

	public void purgeIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		fullTextSession.purgeAll(Collection.class); // remove all taxon base from indexes
		fullTextSession.flushToIndexes();
	}

	public void rebuildIndex() {
		 FullTextSession fullTextSession = Search.getFullTextSession(getSession());
			
			for(Collection collection : list(null,null)) { // re-index all taxon base

				Hibernate.initialize(collection.getSuperCollection());
				Hibernate.initialize(collection.getInstitute());
				fullTextSession.index(collection);
			}
			fullTextSession.flushToIndexes();
	}

	public List<Collection> search(Class<? extends Collection> clazz, String queryString, Integer pageSize, Integer pageNumber,	List<OrderHint> orderHints, List<String> propertyPaths) {
		checkNotInPriorView("CollectionDaoHibernateImpl.search(Class<? extends Collection> clazz, String queryString, Integer pageSize, Integer pageNumber,	List<OrderHint> orderHints, List<String> propertyPaths)");
		QueryParser queryParser = new QueryParser(defaultField, new SimpleAnalyzer());
		List<SpecimenOrObservationBase> results = new ArrayList<SpecimenOrObservationBase>();
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.getFullTextSession(getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = null;
			
			if(clazz == null) {
				fullTextQuery = fullTextSession.createFullTextQuery(query, SpecimenOrObservationBase.class);
			} else {
				fullTextQuery = fullTextSession.createFullTextQuery(query, clazz);
			}
			
			addOrder(fullTextQuery,orderHints);
			
		    if(pageSize != null) {
		    	fullTextQuery.setMaxResults(pageSize);
			    if(pageNumber != null) {
			    	fullTextQuery.setFirstResult(pageNumber * pageSize);
			    } else {
			    	fullTextQuery.setFirstResult(0);
			    }
			}
		    
		    List<Collection> result = (List<Collection>)fullTextQuery.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    return result;

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}

	public String suggestQuery(String string) {
		throw new UnsupportedOperationException("suggestQuery is not supported for Collection");
	}
}