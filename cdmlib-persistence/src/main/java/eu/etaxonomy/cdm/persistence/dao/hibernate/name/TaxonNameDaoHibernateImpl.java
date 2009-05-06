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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

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
	}

	public int countHybridNames(BotanicalName name, HybridRelationshipType type) {
		Query query = null;
		if(type == null) {
			query = getSession().createQuery("select count(relation) from HybridRelationship relation where relation.relatedFrom = :name");
		} else {
			query = getSession().createQuery("select count(relation) from HybridRelationship relation where relation.relatedFrom = :name and relation.type = :type");
			query.setParameter("type", type);
		}
		query.setParameter("name",name);
		return ((Long)query.uniqueResult()).intValue();
	}
	
	public int countNames(String queryString) {
		
        Criteria criteria = getSession().createCriteria(TaxonNameBase.class);
        
		if (queryString != null) {
			criteria.add(Restrictions.ilike("nameCache", queryString));
		}
		criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
		
		return (Integer)criteria.uniqueResult();
	}

	public int countNames(String genusOrUninomial, String infraGenericEpithet,	String specificEpithet, String infraSpecificEpithet, Rank rank) {
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
		criteria.add(Restrictions.eq("rank", rank));
		
		criteria.setProjection(Projections.rowCount());
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
	
	public int countRelatedNames(TaxonNameBase name, NameRelationshipType type) {
		Query query = null;
		if(type == null) {
			query = getSession().createQuery("select count(relation) from NameRelationship relation where relation.relatedFrom = :name");
		} else {
			query = getSession().createQuery("select count(relation) from NameRelationship relation where relation.relatedFrom = :name and relation.type = :type");
			query.setParameter("type", type);
		}
		query.setParameter("name",name);
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countTypeDesignations(TaxonNameBase name, TypeDesignationStatus status) {
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

	public List<HybridRelationship> getHybridNames(BotanicalName name, HybridRelationshipType type, Integer pageSize, Integer pageNumber) {
		Query query = null;
		if(type == null) {
			query = getSession().createQuery("select relation from HybridRelationship relation where relation.relatedFrom = :name");
		} else {
			query = getSession().createQuery("select relation from HybridRelationship relation where relation.relatedFrom = :name and relation.type = :type");
			query.setParameter("type", type);
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
		return (List<HybridRelationship>)query.list();
	}

	public List<NameRelationship> getRelatedNames(TaxonNameBase name, NameRelationshipType type, Integer pageSize, Integer pageNumber) {
		Query query = null;
		if(type == null) {
			query = getSession().createQuery("select relation from NameRelationship relation where relation.relatedFrom = :name");
		} else {
			query = getSession().createQuery("select relation from NameRelationship relation where relation.relatedFrom = :name and relation.type = :type");
			query.setParameter("type", type);
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
		return (List<NameRelationship>)query.list();
	}

	public List<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, TypeDesignationStatus status, Integer pageSize, Integer pageNumber) {
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
		return (List<TypeDesignationBase>)query.list();
	}

	
	public List<TaxonNameBase<?,?>> searchNames(String queryString, MatchMode matchMode, Integer pageSize, Integer pageNumber) {
		
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
	
	
	public List<TaxonNameBase> searchNames(String genusOrUninomial,String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
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
		criteria.add(Restrictions.eq("rank", rank));
		
		if(pageSize != null) {
	    	criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    } else {
		    	criteria.setFirstResult(0);
		    }
		}

		return (List<TaxonNameBase>)criteria.list();
	}

	public List<? extends TaxonNameBase<?,?>> findByName(String queryString, 
			MatchMode matchmode, Integer pageSize, Integer pageNumber, List<Criterion> criteria) {

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
		return results;
	}
	
	public Integer countByName(String queryString, 
			MatchMode matchmode, List<Criterion> criteria) {
		//TODO inprove performance
		List<? extends TaxonNameBase<?,?>> results = findByName(queryString, matchmode, null, null, criteria);
		return results.size();
		
	}
}