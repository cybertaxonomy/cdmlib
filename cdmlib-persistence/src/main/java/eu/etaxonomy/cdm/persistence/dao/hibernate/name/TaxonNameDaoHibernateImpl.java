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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmBase;
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
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 *
 */
@Repository
@Qualifier("taxonNameDaoHibernateImpl")
public class TaxonNameDaoHibernateImpl extends IdentifiableDaoBase<TaxonNameBase> implements ITaxonNameDao {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonNameDaoHibernateImpl.class);

	@Autowired
	private ITaxonDao taxonDao;

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

	public int countHybridNames(NonViralName name, HybridRelationshipType type) {
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
		checkNotInPriorView("countTypeDesignations(TaxonNameBase name, SpecimenTypeDesignationStatus status)");
		Query query = null;
		if(status == null) {
			query = getSession().createQuery("select count(designation) from TypeDesignationBase designation join designation.typifiedNames name where name = :name");
		} else {
			query = getSession().createQuery("select count(designation) from TypeDesignationBase designation join designation.typifiedNames name where name = :name and designation.typeStatus = :status");
			query.setParameter("status", status);
		}
		query.setParameter("name",name);
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<HybridRelationship> getHybridNames(NonViralName name, HybridRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao#getNameRelationships(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction, eu.etaxonomy.cdm.model.name.NameRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
	 */
	public List<NameRelationship> getNameRelationships(TaxonNameBase name, NameRelationship.Direction direction, 
			NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, 
			List<String> propertyPaths) {
		
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(NameRelationship.class);
			if (name != null || direction != null){
				criteria.add(Restrictions.eq(direction.toString(), name));
			}
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
	
	public List<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,	List<String> propertyPaths){
		return getTypeDesignations(name, null, status, pageSize, pageNumber, propertyPaths);
	}
	
	public <T extends TypeDesignationBase> List<T> getTypeDesignations(TaxonNameBase name, 
				Class<T> type,
				TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,
				List<String> propertyPaths){
		checkNotInPriorView("getTypeDesignations(TaxonNameBase name,TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,	List<String> propertyPaths)");
		Query query = null;
		String queryString = "select designation from TypeDesignationBase designation join designation.typifiedNames name where name = :name";

		if(status != null) {
			queryString +=  " and designation.typeStatus = :status";
		}
		if(type != null){
			queryString +=  " and designation.class = :type";
		}

		query = getSession().createQuery(queryString);
		
		if(status != null) {		
			query.setParameter("status", status);
		}
		if(type != null){
			query.setParameter("type", type.getSimpleName());
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
		return defaultBeanInitializer.initializeAll((List<T>)query.list(), propertyPaths);
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
	
	public List<RelationshipBase> getRelationships(Integer limit, Integer start) {
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
	
	
	public Integer countByName(String queryString, MatchMode matchmode, List<Criterion> criteria) {
		//TODO improve performance
		List<? extends TaxonNameBase<?,?>> results = findByName(queryString, matchmode, null, null, criteria, null);
		return results.size();
		
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
				
				list.add(new UuidAndTitleCache(type, uuid, titleCache));
			}
			
			return list;	
		}
	}

	public Integer countByName(Class<? extends TaxonNameBase> clazz,String queryString, MatchMode matchmode, List<Criterion> criteria) {
        return super.countByParam(clazz, "nameCache", queryString, matchmode, criteria);
	}

	public List<TaxonNameBase> findByName(Class<? extends TaxonNameBase> clazz,	String queryString, MatchMode matchmode, List<Criterion> criteria,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
		return super.findByParam(clazz, "nameCache", queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
	}
	
	public UUID delete (TaxonNameBase persistentObject){
		Set<TaxonBase> taxonBases = persistentObject.getTaxonBases();
		super.delete(persistentObject);
				
		for (TaxonBase taxonBase: taxonBases){
			taxonDao.delete(taxonBase);
		}
		return persistentObject.getUuid();
	}
	
	public List<ZoologicalName> getZoologicalNames(Integer limit, Integer start){
		List <TaxonNameBase> names = new ArrayList<TaxonNameBase>();
		List <ZoologicalName> zooNames = new ArrayList<ZoologicalName>();
		names = super.list(ZoologicalName.class, limit, start);
		for (TaxonNameBase name: names){
			zooNames.add((ZoologicalName)name);
		}
		return zooNames;
	}
	
	public ZoologicalName findZoologicalNameByUUID(UUID uuid){
		Criteria criteria = getSession().createCriteria(type);
		if (uuid != null) {
			criteria.add(Restrictions.eq("uuid", uuid));
		} else {
			logger.warn("UUID is NULL");
			return null;
		}
		
		List<? extends TaxonNameBase<?,?>> results = criteria.list();
		if (results.size() == 1) {
			defaultBeanInitializer.initializeAll(results, null);
			TaxonNameBase<?, ?> taxonName = results.iterator().next();
			if (taxonName.isInstanceOf(ZoologicalName.class)) {
				ZoologicalName zoologicalName = CdmBase.deproxy(taxonName, ZoologicalName.class);
				return zoologicalName;
			} else {
				logger.warn("This UUID (" + uuid + ") does not belong to a ZoologicalName. It belongs to: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		} else if (results.size() > 1) {
			logger.error("Multiple results for UUID: " + uuid);
		} else if (results.size() == 0) {
			logger.info("No results for UUID: " + uuid);
		}
		return null;
	}
	
}