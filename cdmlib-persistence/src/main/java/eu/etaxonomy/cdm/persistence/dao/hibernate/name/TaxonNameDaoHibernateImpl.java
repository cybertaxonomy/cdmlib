/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.name.ViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 *
 */
@Repository
@Qualifier("taxonNameDaoHibernateImpl")
public class TaxonNameDaoHibernateImpl 
extends IdentifiableDaoBase<TaxonNameBase> implements ITaxonNameDao {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonNameDaoHibernateImpl.class);

	public TaxonNameDaoHibernateImpl() {
		super(TaxonNameBase.class); 
		indexedClasses = new Class[6];
		indexedClasses[0] = BacterialName.class;
		indexedClasses[1] = BotanicalName.class;
		indexedClasses[2] = CultivarPlantName.class;
		indexedClasses[3] = NonViralName.class;
		indexedClasses[4] = ViralName.class;
		indexedClasses[5] = ZoologicalName.class;
	}
	
	private String defaultField = "titleCache";
	private Class<? extends TaxonNameBase> indexedClasses[]; 

	public int countHybridNames(BotanicalName name, HybridRelationshipType type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Query query = null;
			if(type == null) {
				query = getSession().createQuery("select count(relation) from HybridRelationship relation where relation.relatedFrom = :name");
			} else {
				query = getSession().createQuery("select count(relation) from HybridRelationship relation where relation.relatedFrom = :name and relation.type = :type");
				query.setParameter("type", type);
			}
			query.setParameter("name",name);
			return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(HybridRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedFrom").eq(name.getId()));
			query.addProjection(AuditEntity.id().count("id"));

			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
			}

			return ((Long)query.getSingleResult()).intValue();
		}
	}
	
	public int countNames(String queryString) {
		checkNotInPriorView("TaxonNameDaoHibernateImpl.countNames(String queryString)");
        Criteria criteria = getSession().createCriteria(TaxonNameBase.class);
        
		if (queryString != null) {
			criteria.add(Restrictions.ilike("nameCache", queryString));
		}
		criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
		
		return (Integer)criteria.uniqueResult();
	}

	public int countNames(String queryString, MatchMode matchMode, List<Criterion> criteria) {
		
		Criteria crit = getSession().createCriteria(type);
		if (matchMode == MatchMode.EXACT) {
			crit.add(Restrictions.eq("nameCache", matchMode.queryStringFrom(queryString)));
		} else {
			crit.add(Restrictions.ilike("nameCache", matchMode.queryStringFrom(queryString)));
		}
		if(criteria != null) {
			for (Criterion criterion : criteria) {
				crit.add(criterion);
			}
		}

		crit.setProjection(Projections.projectionList().add(Projections.rowCount()));
		return (Integer)crit.uniqueResult();
	}
	
	public int countNames(String genusOrUninomial, String infraGenericEpithet,	String specificEpithet, String infraSpecificEpithet, Rank rank) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(TaxonNameBase.class);

			/**
			 * Given HHH-2951 - "Restrictions.eq when passed null, should create a NullRestriction"
			 * We need to convert nulls to NullRestrictions for now
			 */
			if(genusOrUninomial != null) {
				criteria.add(Restrictions.eq("genusOrUninomial",genusOrUninomial));
			} else {
				criteria.add(Restrictions.isNull("genusOrUninomial"));
			}

			if(infraGenericEpithet != null) {
				criteria.add(Restrictions.eq("infraGenericEpithet", infraGenericEpithet));
			} else {
				criteria.add(Restrictions.isNull("infraGenericEpithet"));
			}

			if(specificEpithet != null) {
				criteria.add(Restrictions.eq("specificEpithet", specificEpithet));
			} else {
				criteria.add(Restrictions.isNull("specificEpithet"));
			}

			if(infraSpecificEpithet != null) {
				criteria.add(Restrictions.eq("infraSpecificEpithet",infraSpecificEpithet));
			} else {
				criteria.add(Restrictions.isNull("infraSpecificEpithet"));
			}

			if(rank != null) {
				criteria.add(Restrictions.eq("rank", rank));
			}

			criteria.setProjection(Projections.rowCount());
			return (Integer)criteria.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonNameBase.class,auditEvent.getRevisionNumber());

			if(genusOrUninomial != null) {
				query.add(AuditEntity.property("genusOrUninomial").eq(genusOrUninomial));
			} else {
				query.add(AuditEntity.property("genusOrUninomial").isNull());
			}

			if(infraGenericEpithet != null) {
				query.add(AuditEntity.property("infraGenericEpithet").eq(infraGenericEpithet));
			} else {
				query.add(AuditEntity.property("infraGenericEpithet").isNull());
			}

			if(specificEpithet != null) {
				query.add(AuditEntity.property("specificEpithet").eq(specificEpithet));
			} else {
				query.add(AuditEntity.property("specificEpithet").isNull());
			}

			if(infraSpecificEpithet != null) {
				query.add(AuditEntity.property("infraSpecificEpithet").eq(infraSpecificEpithet));
			} else {
				query.add(AuditEntity.property("infraSpecificEpithet").isNull());
			}

			if(rank != null) {
				query.add(AuditEntity.relatedId("rank").eq(rank.getId()));
			}

			query.addProjection(AuditEntity.id().count("id"));
			return ((Long)query.getSingleResult()).intValue();
		}
	}

	public int countNameRelationships(TaxonNameBase name, NameRelationship.Direction direction, NameRelationshipType type) {
		
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Query query = null;
			if(type == null) {
				query = getSession().createQuery("select count(relation) from NameRelationship relation where relation." + direction +" = :name");
			} else {
				query = getSession().createQuery("select count(relation) from NameRelationship relation where relation." + direction +" = :name and relation.type = :type");
				query.setParameter("type", type);
			}
			query.setParameter("name",name);
			return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(NameRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId(direction.toString()).eq(name.getId()));
			query.addProjection(AuditEntity.id().count("id"));

			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
			}

			return ((Long)query.getSingleResult()).intValue();
		}
	}

	public int countTypeDesignations(TaxonNameBase name, SpecimenTypeDesignationStatus status) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Query query = null;
			if(status == null) {
				query = getSession().createQuery("select count(designation) from TypeDesignationBase designation join designation.typifiedNames name where name = :name");
			} else {
				query = getSession().createQuery("select count(designation) from TypeDesignationBase designation join designation.typifiedNames name where name = :name and designation.typeStatus = :status");
				query.setParameter("status", status);
			}
			query.setParameter("name",name);
			return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TypeDesignationBase.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("typifiedNames").eq(name.getId()));
			query.addProjection(AuditEntity.id().count("id"));

			if(type != null) {
				query.add(AuditEntity.relatedId("typeStatus").eq(status.getId()));
			}

			return ((Long)query.getSingleResult()).intValue();
		}
	}

	public List<HybridRelationship> getHybridNames(BotanicalName name, HybridRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(HybridRelationship.class);
			criteria.add(Restrictions.eq("relatedFrom", name));
			if(type != null) {
				criteria.add(Restrictions.eq("type", type));
			}

			if(pageSize != null) {
				criteria.setMaxResults(pageSize);
				if(pageNumber != null) {
					criteria.setFirstResult(pageNumber * pageSize);
				} else {
					criteria.setFirstResult(0);
				}
			}
			
			addOrder(criteria, orderHints);
			
			List<HybridRelationship> results = (List<HybridRelationship>)criteria.list();
			defaultBeanInitializer.initializeAll(results, propertyPaths);
			return results;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(HybridRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedFrom").eq(name.getId()));
			
			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
			}
			
			if(pageSize != null) {
				query.setMaxResults(pageSize);
				if(pageNumber != null) {
					query.setFirstResult(pageNumber * pageSize);
				} else {
					query.setFirstResult(0);
				}
			}

			List<HybridRelationship> results =  (List<HybridRelationship>)query.getResultList();
			defaultBeanInitializer.initializeAll(results, propertyPaths);
			return results;
		}
	}

	public List<NameRelationship> getNameRelationships(TaxonNameBase name, NameRelationship.Direction direction, 
			NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, 
			List<String> propertyPaths) {
		
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(NameRelationship.class);
			criteria.add(Restrictions.eq(direction.toString(), name));
			if(type != null) {
				criteria.add(Restrictions.eq("type", type));
			}

			if(pageSize != null) {
				criteria.setMaxResults(pageSize);
				if(pageNumber != null) {
					criteria.setFirstResult(pageNumber * pageSize);
				} else {
					criteria.setFirstResult(0);
				}
			}
			addOrder(criteria, orderHints);
			
			List<NameRelationship> results = (List<NameRelationship>)criteria.list();
			defaultBeanInitializer.initializeAll(results, propertyPaths);
			return results;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(NameRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId(direction.toString()).eq(name.getId()));

			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
			}
			
			if(pageSize != null) {
				query.setMaxResults(pageSize);
				if(pageNumber != null) {
					query.setFirstResult(pageNumber * pageSize);
				} else {
					query.setFirstResult(0);
				}
			}

			List<NameRelationship> results = (List<NameRelationship>)query.getResultList();
			defaultBeanInitializer.initializeAll(results, propertyPaths);
			return results;
		}
	}

	public List<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, 
			TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber) {
		return getTypeDesignations(name, status, pageSize, pageNumber, null);
	}
	
	public List<TypeDesignationBase> getTypeDesignations(TaxonNameBase name,
				TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,
				List<String> propertyPaths){
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Query query = null;
			if(status == null) {
				query = getSession().createQuery("select designation from TypeDesignationBase designation join designation.typifiedNames name where name = :name");
			} else {
				query = getSession().createQuery("select designation from TypeDesignationBase designation join designation.typifiedNames name where name = :name and designation.typeStatus = :status");
				query.setParameter("status", status);
			}
			query.setParameter("name",name);

			if(pageSize != null) {
				query.setMaxResults(pageSize);
				if(pageNumber != null) {
					query.setFirstResult(pageNumber * pageSize);
				} else {
					query.setFirstResult(0);
				}
			}
			return defaultBeanInitializer.initializeAll((List<TypeDesignationBase>)query.list(), propertyPaths);
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TypeDesignationBase.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("typifiedNames").eq(name.getId()));

			if(type != null) {
				query.add(AuditEntity.relatedId("typeStatus").eq(status.getId()));
			}
			
			if(pageSize != null) {
				query.setMaxResults(pageSize);
				if(pageNumber != null) {
					query.setFirstResult(pageNumber * pageSize);
				} else {
					query.setFirstResult(0);
				}
			}

			return (List<TypeDesignationBase>)query.getResultList();
		}
	}

	
	public List<TaxonNameBase<?,?>> searchNames(String queryString, MatchMode matchMode, Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("TaxonNameDaoHibernateImpl.searchNames(String queryString, Integer pageSize, Integer pageNumber)");
		Criteria criteria = getSession().createCriteria(TaxonNameBase.class);

		if (queryString != null) {
			criteria.add(Restrictions.ilike("nameCache", queryString));
		}
		if(pageSize != null) {
	    	criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    } else {
		    	criteria.setFirstResult(0);
		    }
		}
		List<TaxonNameBase<?,?>> results = criteria.list();
		return results;
	}

	
	public List<TaxonNameBase<?,?>> searchNames(String queryString, Integer pageSize, Integer pageNumber) {
		return searchNames(queryString, MatchMode.BEGINNING, pageSize, pageNumber);
	}
	
	
	public List<TaxonNameBase> searchNames(String genusOrUninomial,String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,
			List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(TaxonNameBase.class);

			/**
			 * Given HHH-2951 - "Restrictions.eq when passed null, should create a NullRestriction"
			 * We need to convert nulls to NullRestrictions for now
			 */
			if(genusOrUninomial != null) {
				criteria.add(Restrictions.eq("genusOrUninomial",genusOrUninomial));
			} else {
				criteria.add(Restrictions.isNull("genusOrUninomial"));
			}

			if(infraGenericEpithet != null) {
				criteria.add(Restrictions.eq("infraGenericEpithet", infraGenericEpithet));
			} else {
				criteria.add(Restrictions.isNull("infraGenericEpithet"));
			}

			if(specificEpithet != null) {
				criteria.add(Restrictions.eq("specificEpithet", specificEpithet));
			} else {
				criteria.add(Restrictions.isNull("specificEpithet"));
			}

			if(infraSpecificEpithet != null) {
				criteria.add(Restrictions.eq("infraSpecificEpithet",infraSpecificEpithet));
			} else {
				criteria.add(Restrictions.isNull("infraSpecificEpithet"));
			}
			
			if(rank != null) {
			    criteria.add(Restrictions.eq("rank", rank));
			}

			if(pageSize != null) {
				criteria.setMaxResults(pageSize);
				if(pageNumber != null) {
					criteria.setFirstResult(pageNumber * pageSize);
				} else {
					criteria.setFirstResult(0);
				}
			}
			
			addOrder(criteria, orderHints);

			List<TaxonNameBase> results = (List<TaxonNameBase>)criteria.list();
			defaultBeanInitializer.initializeAll(results, propertyPaths);
			return results;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonNameBase.class,auditEvent.getRevisionNumber());

			if(genusOrUninomial != null) {
				query.add(AuditEntity.property("genusOrUninomial").eq(genusOrUninomial));
			} else {
				query.add(AuditEntity.property("genusOrUninomial").isNull());
			}

			if(infraGenericEpithet != null) {
				query.add(AuditEntity.property("infraGenericEpithet").eq(infraGenericEpithet));
			} else {
				query.add(AuditEntity.property("infraGenericEpithet").isNull());
			}

			if(specificEpithet != null) {
				query.add(AuditEntity.property("specificEpithet").eq(specificEpithet));
			} else {
				query.add(AuditEntity.property("specificEpithet").isNull());
			}

			if(infraSpecificEpithet != null) {
				query.add(AuditEntity.property("infraSpecificEpithet").eq(infraSpecificEpithet));
			} else {
				query.add(AuditEntity.property("infraSpecificEpithet").isNull());
			}

			if(rank != null) {
				query.add(AuditEntity.relatedId("rank").eq(rank.getId()));
			}

			if(pageSize != null) {
				query.setMaxResults(pageSize);
				if(pageNumber != null) {
					query.setFirstResult(pageNumber * pageSize);
				} else {
					query.setFirstResult(0);
				}
			}
			
			List<TaxonNameBase> results = (List<TaxonNameBase>)query.getResultList();
			defaultBeanInitializer.initializeAll(results, propertyPaths);
			return results;
		}
	}

	public List<? extends TaxonNameBase<?,?>> findByName(String queryString, 
			MatchMode matchmode, Integer pageSize, Integer pageNumber, List<Criterion> criteria, List<String> propertyPaths) {

		Criteria crit = getSession().createCriteria(type);
		if (matchmode == MatchMode.EXACT) {
			crit.add(Restrictions.eq("nameCache", matchmode.queryStringFrom(queryString)));
		} else {
			crit.add(Restrictions.ilike("nameCache", matchmode.queryStringFrom(queryString)));
		}
		if(criteria != null){
			for (Criterion criterion : criteria) {
				crit.add(criterion);
			}
		}
		crit.addOrder(Order.asc("nameCache"));

		if(pageSize != null) {
			crit.setMaxResults(pageSize);
			if(pageNumber != null) {
				crit.setFirstResult(pageNumber * pageSize);
			}
		}

		List<? extends TaxonNameBase<?,?>> results = crit.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		
		return results;
	}
	
	public List<RelationshipBase> getAllRelationships(Integer limit, Integer start) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    //FIXME only NameRelationships
			Criteria criteria = getSession().createCriteria(RelationshipBase.class);
		    return (List<RelationshipBase>)criteria.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(RelationshipBase.class,auditEvent.getRevisionNumber());
			return (List<RelationshipBase>)query.getResultList();
		}
	}
	
	
	public Integer countByName(String queryString, 
			MatchMode matchmode, List<Criterion> criteria) {
		//TODO improve performance
		List<? extends TaxonNameBase<?,?>> results = findByName(queryString, matchmode, null, null, criteria, null);
		return results.size();
		
	}


	public int count(Class<? extends TaxonNameBase> clazz, String queryString) {
		checkNotInPriorView("TaxonNameDaoHibernateImpl.count(String queryString, Boolean accepted)");
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
		
		for(TaxonNameBase name : list(null,null)) { // re-index all taxon base
			fullTextSession.index(name);
		}
		fullTextSession.flushToIndexes();
	}

	public List<TaxonNameBase> search(Class<? extends TaxonNameBase> clazz,	String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		checkNotInPriorView("TaxonNameDaoHibernateImpl.searchTaxa(String queryString, Boolean accepted,	Integer pageSize, Integer pageNumber)");
		QueryParser queryParser = new QueryParser(defaultField, new SimpleAnalyzer());
		List<TaxonNameBase> results = new ArrayList<TaxonNameBase>();
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.getFullTextSession(getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = null;
			
			if(clazz == null) {
				fullTextQuery = fullTextSession.createFullTextQuery(query, TaxonNameBase.class);
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
		    
		    List<TaxonNameBase> result = (List<TaxonNameBase>)fullTextQuery.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    return result;

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}

	public String suggestQuery(String string) {
		throw new UnsupportedOperationException("suggestQuery is not supported for TaxonNameBase");
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao#getUuidAndTitleCacheOfNames()
	 */
	public List<UuidAndTitleCache> getUuidAndTitleCacheOfNames() {
		String queryString = "SELECT uuid, fullTitleCache FROM TaxonNameBase";
		
		List<Object[]> result = getSession().createSQLQuery(queryString).list();
				
		if(result.size() == 0){
			return null;
		}else{
			List<UuidAndTitleCache> list = new ArrayList<UuidAndTitleCache>(result.size()); 
			
			for (Object object : result){
				
				Object[] objectArray = (Object[]) object;
				
				UUID uuid = UUID.fromString((String) objectArray[0]);
				String titleCache = (String) objectArray[1];
				
				list.add(new UuidAndTitleCache(uuid, titleCache));
			}
			
			return list;	
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao#list(java.lang.Class, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
	 */
	public <TYPE extends TaxonNameBase> List<TYPE> list(Class<TYPE> type,
			Integer limit, Integer start, List<OrderHint> orderHints,
			List<String> propertyPaths) {
		// TODO Auto-generated method stub
		return null;
	}
}