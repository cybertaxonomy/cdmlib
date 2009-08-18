/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.media;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @created 08.09.2008
 */
@Repository
public class MediaDaoHibernateImpl extends AnnotatableDaoImpl<Media> implements IMediaDao {

	private String defaultField = "title.text";
	private Class<? extends Media> indexedClasses[]; 
	
	public MediaDaoHibernateImpl() {
		super(Media.class);
		indexedClasses = new Class[3];
		indexedClasses[0] = Media.class;
		indexedClasses[1] = MediaKey.class;
		indexedClasses[2] = PhylogeneticTree.class;
	}

	public int countMediaKeys(Set<Taxon> taxonomicScope,	Set<NamedArea> geoScopes) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(MediaKey.class);
			
			if(taxonomicScope != null && !taxonomicScope.isEmpty()) {
				Set<Integer> taxonomicScopeIds = new HashSet<Integer>();
				for(Taxon n : taxonomicScope) {
					taxonomicScopeIds.add(n.getId());
				}
				criteria.createCriteria("taxonomicScope").add(Restrictions.in("id", taxonomicScopeIds));
			}
			
			if(geoScopes != null && !geoScopes.isEmpty()) {
				Set<Integer> geoScopeIds = new HashSet<Integer>();
				for(NamedArea n : geoScopes) {
					geoScopeIds.add(n.getId());
				}
				criteria.createCriteria("geographicalScope").add(Restrictions.in("id", geoScopeIds));
			}
			
			criteria.setProjection(Projections.countDistinct("id"));
			
			return (Integer)criteria.uniqueResult();
		} else {
			if((taxonomicScope == null || taxonomicScope.isEmpty()) && (geoScopes == null || geoScopes.isEmpty())) {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(MediaKey.class,auditEvent.getRevisionNumber());
				query.addProjection(AuditEntity.id().count("id"));
				return ((Long)query.getSingleResult()).intValue();
			} else {
				throw new OperationNotSupportedInPriorViewException("countMediaKeys(Set<Taxon> taxonomicScope,	Set<NamedArea> geoScopes)");
			}
		}
	}

	public List<MediaKey> getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria inner = getSession().createCriteria(MediaKey.class);

			if(taxonomicScope != null && !taxonomicScope.isEmpty()) {
				Set<Integer> taxonomicScopeIds = new HashSet<Integer>();
				for(Taxon n : taxonomicScope) {
					taxonomicScopeIds.add(n.getId());
				}
				inner.createCriteria("taxonomicScope").add(Restrictions.in("id", taxonomicScopeIds));
			}

			if(geoScopes != null && !geoScopes.isEmpty()) {
				Set<Integer> geoScopeIds = new HashSet<Integer>();
				for(NamedArea n : geoScopes) {
					geoScopeIds.add(n.getId());
				}
				inner.createCriteria("geographicalScope").add(Restrictions.in("id", geoScopeIds));
			}

			inner.setProjection(Projections.distinct(Projections.id()));

			Criteria criteria = getSession().createCriteria(MediaKey.class);
			criteria.add(Restrictions.in("id", (List<Integer>)inner.list()));

			if(pageSize != null) {
				criteria.setMaxResults(pageSize);
				if(pageNumber != null) {
					criteria.setFirstResult(pageNumber * pageSize);
				}
			}

			List<MediaKey> results = (List<MediaKey>)criteria.list();

			defaultBeanInitializer.initializeAll(results, propertyPaths);

			return results;
		} else {
			if((taxonomicScope == null || taxonomicScope.isEmpty()) && (geoScopes == null || geoScopes.isEmpty())) {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(MediaKey.class,auditEvent.getRevisionNumber());
				
				if(pageSize != null) {
			        query.setMaxResults(pageSize);
			        if(pageNumber != null) {
			            query.setFirstResult(pageNumber * pageSize);
			        } else {
			    	    query.setFirstResult(0);
			        }
			    }
				List<MediaKey> results = (List<MediaKey>)query.getResultList();
				defaultBeanInitializer.initializeAll(results, propertyPaths);
				return results;
			} else {
				throw new OperationNotSupportedInPriorViewException("getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
			}
		}
	}
	
	public List<Rights> getRights(Media media, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		checkNotInPriorView("MediaDaoHibernateImpl.getRights(Media t, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
		Query query = getSession().createQuery("select rights from Media media join media.rights rights where media = :media");
		query.setParameter("media",media);
		setPagingParameter(query, pageSize, pageNumber);
		List<Rights> results = (List<Rights>)query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}
	
	public int countRights(Media media) {
		checkNotInPriorView("MediaDaoHibernateImpl.countRights(Media t)");
		Query query = getSession().createQuery("select count(rights) from Media media join media.rights rights where media = :media");
		query.setParameter("media",media);
		return ((Long)query.uniqueResult()).intValue();
	}

	public int count(Class<? extends Media> clazz, String queryString) {
		checkNotInPriorView("MediaDaoHibernateImpl.count(String queryString, Boolean accepted)");
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
		for(Class clazz : indexedClasses) {
	        searchFactory.optimize(clazz); // optimize the indices ()
		}
	    fullTextSession.flushToIndexes();
	}

	public void purgeIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		for(Class clazz : indexedClasses) {
		    fullTextSession.purgeAll(clazz); // remove all taxon base from indexes
		}
		fullTextSession.flushToIndexes();
	}

	public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		
		for(Media media : list(null,null)) { // re-index all media
			Hibernate.initialize(media.getTitle());
			Hibernate.initialize(media.getDescription());
			Hibernate.initialize(media.getArtist());
			fullTextSession.index(media);
		}
		fullTextSession.flushToIndexes();
	}

	public List<Media> search(Class<? extends Media> clazz, String queryString,	Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths) {
		checkNotInPriorView("MediaDaoHibernateImpl.searchTaxa(String queryString, Boolean accepted,	Integer pageSize, Integer pageNumber)");
		QueryParser queryParser = new QueryParser(defaultField, new SimpleAnalyzer());
		List<Media> results = new ArrayList<Media>();
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.getFullTextSession(getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = null;
			
			if(clazz == null) {
				fullTextQuery = fullTextSession.createFullTextQuery(query, type);
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
		    
		    List<Media> result = (List<Media>)fullTextQuery.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    return result;

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}

	public String suggestQuery(String string) {
		throw new UnsupportedOperationException("suggestQuery is not supported for Media");
	}
}
