/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.hibernate.AlternativeSpellingSuggestionParser;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.SelectMode;


/**
 * @author a.mueller
 * @created 24.11.2008
 * @version 1.0
 */
@Repository
@Qualifier("taxonDaoHibernateImpl")
public class TaxonDaoHibernateImpl extends IdentifiableDaoBase<TaxonBase> implements ITaxonDao {	
	private AlternativeSpellingSuggestionParser<TaxonBase> alternativeSpellingSuggestionParser;
	
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);
		
	private String defaultField = "name.titleCache";
	private Class<? extends TaxonBase> indexedClasses[]; 

	public TaxonDaoHibernateImpl() {
		super(TaxonBase.class);
		indexedClasses = new Class[2];
		indexedClasses[0] = Taxon.class;
		indexedClasses[1] = Synonym.class;
	}
	
	@Autowired(required = false)   //TODO switched of because it caused problems when starting CdmApplicationController
	public void setAlternativeSpellingSuggestionParser(AlternativeSpellingSuggestionParser<TaxonBase> alternativeSpellingSuggestionParser) {
		this.alternativeSpellingSuggestionParser = alternativeSpellingSuggestionParser;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec) {
		return getRootTaxa(sec, CdmFetch.FETCH_CHILDTAXA(), true, false);
	}
		
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.name.Rank, eu.etaxonomy.cdm.model.reference.ReferenceBase, eu.etaxonomy.cdm.persistence.fetch.CdmFetch, java.lang.Boolean, java.lang.Boolean)
	 */
	public List<Taxon> getRootTaxa(Rank rank, ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications, List<String> propertyPaths) {
		checkNotInPriorView("TaxonDaoHibernateImpl.getRootTaxa(Rank rank, ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications)");
		if (onlyWithChildren == null){
			onlyWithChildren = true;
		}
		if (withMisapplications == null){
			withMisapplications = true;
		}
		if (cdmFetch == null){
			cdmFetch = CdmFetch.NO_FETCH();
		}

		Criteria crit = getSession().createCriteria(Taxon.class);
		
		crit.setFetchMode("name", FetchMode.JOIN);
		crit.createAlias("name", "name");
		
		if (rank != null) {
			crit.add(Restrictions.eq("name.rank", rank));
		}else{
			crit.add(Restrictions.isNull("taxonomicParentCache"));
		}

		if (sec != null){
			crit.add(Restrictions.eq("sec", sec) );
		}

		if (! cdmFetch.includes(CdmFetch.FETCH_CHILDTAXA())){
			logger.info("Not fetching child taxa");
			//TODO overwrite LAZY (SELECT) does not work (bug in hibernate?)
			crit.setFetchMode("relationsToThisTaxon.fromTaxon", FetchMode.LAZY);
		}

		List<Taxon> results = new ArrayList<Taxon>();
		List<Taxon> taxa = crit.list();
		for(Taxon taxon : taxa){
			
			
			//childTaxa
			//TODO create restriction instead
			// (a) not using cache fields
			/*Hibernate.initialize(taxon.getRelationsFromThisTaxon());
			if (onlyWithChildren == false || taxon.getRelationsFromThisTaxon().size() > 0){
				if (withMisapplications == true || ! taxon.isMisappliedName()){
					defaultBeanInitializer.initialize(taxon, propertyPaths);
					results.add(taxon);
				}
			}*/
			// (b) using cache fields
			if (onlyWithChildren == false || taxon.hasTaxonomicChildren()){
				if (withMisapplications == true || ! taxon.isMisappliedName()){
					defaultBeanInitializer.initialize(taxon, propertyPaths);
					results.add(taxon);
				}
			}
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase, eu.etaxonomy.cdm.persistence.fetch.CdmFetch, java.lang.Boolean, java.lang.Boolean)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications) {
		return getRootTaxa(null, sec, cdmFetch, onlyWithChildren, withMisapplications, null);
	}
	

	public List<TaxonBase> getTaxaByName(String queryString, ReferenceBase sec) {
		
		return getTaxaByName(queryString, true, sec);
	}

	public List<TaxonBase> getTaxaByName(String queryString, Boolean accepted, ReferenceBase sec) {
		checkNotInPriorView("TaxonDaoHibernateImpl.getTaxaByName(String name, ReferenceBase sec)");
		
        Criteria criteria = null;
		if (accepted == true) {
			criteria = getSession().createCriteria(Taxon.class);
		} else {
			criteria = getSession().createCriteria(Synonym.class);
		}
		
		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		if (sec != null && sec.getId() != 0) {
			criteria.add(Restrictions.eq("sec", sec ) );
		}

		if (queryString != null) {
			criteria.add(Restrictions.ilike("name.nameCache", queryString));
		}

		return (List<TaxonBase>)criteria.list();
	}

	public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode, SelectMode selectMode,
			Integer pageSize, Integer pageNumber) {
		
		return getTaxaByName(queryString, matchMode, selectMode, null, pageSize, pageNumber);
	}
	
	public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode, 
			Boolean accepted, Integer pageSize, Integer pageNumber) {
		
		if (accepted == true) {
			return getTaxaByName(queryString, matchMode, SelectMode.TAXA, pageSize, pageNumber);
		} else {
			return getTaxaByName(queryString, matchMode, SelectMode.SYNONYMS, pageSize, pageNumber);
		}
	}
	
	
	public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode, SelectMode selectMode,
			ReferenceBase sec, Integer pageSize, Integer pageNumber) {

		Criteria criteria = null;
		Class<?> clazz = selectMode.criteria();
		criteria = getSession().createCriteria(clazz);
		
		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		if (queryString != null) {
			String hqlQueryString = matchMode.queryStringFrom(queryString);
			if (matchMode == MatchMode.EXACT) {
				criteria.add(Restrictions.eq("name.nameCache", hqlQueryString));
			} else {
				criteria.add(Restrictions.ilike("name.nameCache", hqlQueryString));
			}
		}
		
		if (sec != null && sec.getId() != 0) {
			criteria.add(Restrictions.eq("sec", sec ) );
		}
		
		criteria.addOrder(Order.asc("name.nameCache"));
		 
		if(pageSize != null) {
			criteria.setMaxResults(pageSize);
			if(pageNumber != null) {
				criteria.setFirstResult(pageNumber * pageSize);
			}
		}

		List<TaxonBase> results = criteria.list();
		return results;
		
	}
	
	public Integer countTaxaByName(String queryString, MatchMode matchMode, SelectMode selectMode) {
		
		return countTaxaByName(queryString, matchMode, selectMode, null);
	}

	public Integer countTaxaByName(String queryString, 
			MatchMode matchMode, SelectMode selectMode, ReferenceBase sec) {

		Criteria criteria = null;
		Class<?> clazz = selectMode.criteria();
		criteria = getSession().createCriteria(clazz);

		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");

		if (queryString != null) {
			String hqlQueryString = matchMode.queryStringFrom(queryString);
			if (matchMode == MatchMode.EXACT) {
				criteria.add(Restrictions.eq("name.nameCache", hqlQueryString));
			} else {
				criteria.add(Restrictions.ilike("name.nameCache", hqlQueryString));
			}
		}

		if (sec != null && sec.getId() != 0) {
			criteria.add(Restrictions.eq("sec", sec ) );
		}

		criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
		return (Integer)criteria.uniqueResult();

	}
	
	public Integer countTaxaByName(String queryString, MatchMode matchMode, Boolean accepted) {
		
		Criteria criteria = null;
		if (accepted == true) {
			criteria = getSession().createCriteria(Taxon.class);
		} else {
			criteria = getSession().createCriteria(Synonym.class);
		}

		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		if (matchMode == MatchMode.EXACT) {
			criteria.add(Restrictions.eq("name.nameCache", matchMode.queryStringFrom(queryString)));
		} else {
			criteria.add(Restrictions.ilike("name.nameCache", matchMode.queryStringFrom(queryString)));
		}
		
		criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
		return (Integer)criteria.uniqueResult();
	}
	

	public List<TaxonBase> getAllTaxonBases(Integer pagesize, Integer page) {
		return super.list(pagesize, page);
	}

	public List<Synonym> getAllSynonyms(Integer limit, Integer start) {
		return super.list(Synonym.class, limit, start);
	}

	public List<Taxon> getAllTaxa(Integer limit, Integer start) {
		return super.list(Taxon.class, limit, start);
	}

	public List<RelationshipBase> getAllRelationships(Integer limit, Integer start) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria criteria = getSession().createCriteria(RelationshipBase.class);
		    return (List<RelationshipBase>)criteria.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(RelationshipBase.class,auditEvent.getRevisionNumber());
			return (List<RelationshipBase>)query.getResultList();
		}
	}
	
	/** Sets the taxonomic parent to null. Does not handle taxonomic relationships. */
//	private boolean nullifyTaxonomicParent(Taxon taxon) {
//
//		try {
//			Method nullifyTaxonomicParent = taxon.getClass().getMethod("nullifyTaxonomicParent");
//			nullifyTaxonomicParent.invoke(taxon);
//		} catch (NoSuchMethodException ex) {
//			logger.error("NoSuchMethod: " + ex.getMessage());
//			return false;
//		} catch (IllegalArgumentException ex) {
//			logger.error("IllegalArgumentException: " + ex.getMessage());
//			return false;
//		} catch (IllegalAccessException ex) {
//			logger.error("IllegalAccessException: " + ex.getMessage());
//			return false;
//		} catch (InvocationTargetException ex) {
//			logger.error("IllegalAccessException: " + ex.getMessage());
//			return false;
//		}
//		return true;
//	}
	
	@Override
	public UUID delete(TaxonBase taxonBase) throws DataAccessException{
		if (taxonBase == null){
			logger.warn("TaxonBase was 'null'");
			return null;
		}
		
		// Merge the object in if it is detached
		//
		// I think this is preferable to catching lazy initialization errors 
		// as that solution only swallows and hides the exception, but doesn't 
		// actually solve it.
		getSession().merge(taxonBase);
		
		for(Iterator<Annotation> iterator = taxonBase.getAnnotations().iterator(); iterator.hasNext();) {
			Annotation annotation = iterator.next();
		    annotation.setAnnotatedObj(null);
		    iterator.remove();
		    getSession().delete(annotation);
	    }
		
		for(Iterator<Marker> iterator = taxonBase.getMarkers().iterator(); iterator.hasNext();) {
			Marker marker = iterator.next();
		    marker.setMarkedObj(null);
		    iterator.remove();
		    getSession().delete(marker);
	    }
		
		for(Iterator<Extension> iterator = taxonBase.getExtensions().iterator(); iterator.hasNext();) {
			Extension extension = iterator.next();
		    extension.setExtendedObj(null);
		    iterator.remove();
		    getSession().delete(extension);
	    }
		
		for(Iterator<OriginalSource> iterator = taxonBase.getSources().iterator(); iterator.hasNext();) {
			OriginalSource source = iterator.next();
		    source.setSourcedObj(null);
		    iterator.remove();
		    getSession().delete(source);
	    }

		for(Iterator<Rights> iterator = taxonBase.getRights().iterator(); iterator.hasNext();) {
			Rights rights = iterator.next();
		    iterator.remove();
		    getSession().delete(rights);
	    }
		
		if (taxonBase instanceof Taxon){ //	is Taxon
			//taxonRelationships
			Taxon taxon = (Taxon)taxonBase;
						
			for (Iterator<TaxonRelationship> iterator = taxon.getRelationsFromThisTaxon().iterator(); iterator.hasNext();){
				TaxonRelationship relationFromThisTaxon = iterator.next();
				iterator.remove();
				
				// decrease children count of taxonomic parent by one
				if (relationFromThisTaxon.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
					Taxon toTaxon = relationFromThisTaxon.getToTaxon(); // parent
					if (toTaxon != null) {
						toTaxon.setTaxonomicChildrenCount(toTaxon.getTaxonomicChildrenCount() - 1);	
					}
				}
				relationFromThisTaxon.setToTaxon(null);
				relationFromThisTaxon.setFromTaxon(null);
				getSession().delete(relationFromThisTaxon);
			}
			
			for (Iterator<TaxonRelationship> iterator = taxon.getRelationsToThisTaxon().iterator(); iterator.hasNext();){
				TaxonRelationship relationToThisTaxon = iterator.next();
				iterator.remove();
				
                // set parent cache of child to null
				if (relationToThisTaxon.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
					Taxon fromTaxon = relationToThisTaxon.getFromTaxon(); // child
					if (fromTaxon != null) {
						fromTaxon.nullifyTaxonomicParent();
					}
				}
				relationToThisTaxon.setFromTaxon(null);
				relationToThisTaxon.setToTaxon(null);
				getSession().delete(relationToThisTaxon);
			}
			
			//SynonymRelationships
			for (Iterator<SynonymRelationship> iterator = taxon.getSynonymRelations().iterator(); iterator.hasNext();){
				SynonymRelationship synonymRelation = iterator.next();
				iterator.remove();
				synonymRelation.setAcceptedTaxon(null);
				synonymRelation.setSynonym(null);
				getSession().delete(synonymRelation);
			} 
			
			// Descriptions
			for (Iterator<TaxonDescription> iterDesc = taxon.getDescriptions().iterator(); iterDesc.hasNext();) {
				TaxonDescription taxonDescription = iterDesc.next();
				iterDesc.remove();
				//taxonDescription.setTaxon(null);
				Field field = ReflectionUtils.findField(TaxonDescription.class, "taxon", Taxon.class);
				ReflectionUtils.makeAccessible(field);
				ReflectionUtils.setField(field, taxonDescription, null);
				for (Iterator<DescriptionElementBase> iterDescElem = 
					taxonDescription.getElements().iterator(); iterDescElem.hasNext();) {
					DescriptionElementBase descriptionElement = iterDescElem.next();
					iterDescElem.remove();
					getSession().delete(descriptionElement);
				}
				getSession().delete(taxonDescription);
			}
			
			taxon.nullifyTaxonomicParent();

		} else { //is Synonym
			Synonym synonym = (Synonym)taxonBase;
			for (Iterator<SynonymRelationship> iterator = synonym.getSynonymRelations().iterator(); iterator.hasNext();){
				SynonymRelationship synonymRelation = iterator.next();
				iterator.remove();
				synonymRelation.setAcceptedTaxon(null);
				synonymRelation.setSynonym(null);
			} ;
		}
		return super.delete(taxonBase);
	}


	// TODO add generic return type !!
	public List findByName(String queryString, MatchMode matchMode, int page, int pagesize, boolean onlyAcccepted) {
		ArrayList<Criterion> criteria = new ArrayList<Criterion>();
		//TODO ... Restrictions.eq(propertyName, value)
		return super.findByTitle(queryString, matchMode, page, pagesize, criteria);

	}

	public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted) {
		checkNotInPriorView("TaxonDaoHibernateImpl.countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted)");
		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
		crit.setProjection(Projections.rowCount());
		int result = ((Integer)crit.list().get(0)).intValue();
		return result;
	}


	public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted, List<Criterion> criteria) {
		checkNotInPriorView("TaxonDaoHibernateImpl.countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted, List<Criterion> criteria)");
		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
		if(criteria != null){
			for (Criterion criterion : criteria) {
				crit.add(criterion);
			}
		}
		crit.setProjection(Projections.rowCount());
		int result = ((Integer)crit.list().get(0)).intValue();
		return result;
	}

	public int countRelatedTaxa(Taxon taxon, TaxonRelationshipType type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = null;
		
		    if(type == null) {
			    query = getSession().createQuery("select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship.relatedTo = :relatedTo");
		    } else {
			    query = getSession().createQuery("select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship.relatedTo = :relatedTo and taxonRelationship.type = :type");
			    query.setParameter("type",type);
		    }
		
		    query.setParameter("relatedTo", taxon);
		
		    return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));
			query.addProjection(AuditEntity.id().count("id"));
			
			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
			
			return ((Long)query.getSingleResult()).intValue();
		}
	}

	public int countSynonyms(Taxon taxon, SynonymRelationshipType type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Query query = null;

			if(type == null) {
				query = getSession().createQuery("select count(synonymRelationship) from SynonymRelationship synonymRelationship where synonymRelationship.relatedTo = :relatedTo");
			} else {
				query = getSession().createQuery("select count(synonymRelationship) from SynonymRelationship synonymRelationship where synonymRelationship.relatedTo = :relatedTo and synonymRelationship.type = :type");
				query.setParameter("type",type);
			}

			query.setParameter("relatedTo", taxon);

			return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));
			query.addProjection(AuditEntity.id().count("id"));
			
			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
			
			return ((Long)query.getSingleResult()).intValue();
		}
	}

	public int count(Class<? extends TaxonBase> clazz, String queryString) {
		checkNotInPriorView("TaxonDaoHibernateImpl.count(String queryString, Boolean accepted)");
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
	
	public int countTaxaByName(String queryString, Boolean accepted, ReferenceBase sec) {
		checkNotInPriorView("TaxonDaoHibernateImpl.countTaxaByName(String queryString, Boolean accepted, ReferenceBase sec)");
		Criteria criteria = null;
		
		if (accepted == true) {
			criteria = getSession().createCriteria(Taxon.class);
		} else {
			criteria = getSession().createCriteria(Synonym.class);
		}
		
		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");

		if (sec != null){
			if(sec.getId() == 0){
				getSession().save(sec);
			}
			criteria.add(Restrictions.eq("sec", sec ) );
		}
		if (queryString != null) {
			criteria.add(Restrictions.ilike("name.nameCache", queryString));
		}
		criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
		
		return (Integer)criteria.uniqueResult();
	}

	public int countTaxaByName(Class<? extends TaxonBase> clazz, String genusOrUninomial,	String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank) {
		checkNotInPriorView("TaxonDaoHibernateImpl.countTaxaByName(Boolean accepted, String genusOrUninomial,	String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank)");
        Criteria criteria = null;
		
		if(clazz == null) {
			criteria = getSession().createCriteria(TaxonBase.class);
		} else {
			criteria = getSession().createCriteria(clazz);		
		}
		
		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		if(genusOrUninomial != null) {
			criteria.add(Restrictions.eq("name.genusOrUninomial", genusOrUninomial));
		}
		
		if(infraGenericEpithet != null) {
			criteria.add(Restrictions.eq("name.infraGenericEpithet", infraGenericEpithet));
		}
		
		if(specificEpithet != null) {
			criteria.add(Restrictions.eq("name.specificEpithet", specificEpithet));
		}
		
		if(infraSpecificEpithet != null) {
			criteria.add(Restrictions.eq("name.infraSpecificEpithet", infraSpecificEpithet));
		}
		
		if(rank != null) {
			criteria.add(Restrictions.eq("name.rank", rank));
		}
		
		criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
	
		return (Integer)criteria.uniqueResult();
	}

	public List<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber) {
		checkNotInPriorView("TaxonDaoHibernateImpl.findTaxaByName(Boolean accepted, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber)");
		Criteria criteria = null;
		
		if(clazz == null) {
			criteria = getSession().createCriteria(TaxonBase.class);
		} else {
			criteria = getSession().createCriteria(clazz);
		}
		
		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		if(genusOrUninomial == null) {
			criteria.add(Restrictions.isNull("name.genusOrUninomial"));
		} else if(!genusOrUninomial.equals("*")) {
			criteria.add(Restrictions.eq("name.genusOrUninomial", genusOrUninomial));
		}
		
		if(infraGenericEpithet == null) {
			criteria.add(Restrictions.isNull("name.infraGenericEpithet"));
		} else if(!infraGenericEpithet.equals("*")) {
			criteria.add(Restrictions.eq("name.infraGenericEpithet", infraGenericEpithet));
		} 
		
		if(specificEpithet == null) {
			criteria.add(Restrictions.isNull("name.specificEpithet"));
		} else if(!specificEpithet.equals("*")) {
			criteria.add(Restrictions.eq("name.specificEpithet", specificEpithet));
			
		}
		
		if(infraSpecificEpithet == null) {
			criteria.add(Restrictions.isNull("name.infraSpecificEpithet"));
		} else if(!infraSpecificEpithet.equals("*")) {
			criteria.add(Restrictions.eq("name.infraSpecificEpithet", infraSpecificEpithet));
		}
		
		if(rank != null) {
			criteria.add(Restrictions.eq("name.rank", rank));
		}
		
		if(pageSize != null) {
	    	criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    } else {
		    	criteria.setFirstResult(0);
		    }
		}
	
		return (List<TaxonBase>)criteria.list();
	}

	public List<TaxonRelationship> getRelatedTaxa(Taxon taxon,	TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(TaxonRelationship.class);
            
			criteria.add(Restrictions.eq("relatedTo", taxon));
		    if(type != null) {
		    	criteria.add(Restrictions.eq("type", type));
		    } 
		
            addOrder(criteria,orderHints);
		
		    if(pageSize != null) {
		    	criteria.setMaxResults(pageSize);
		        if(pageNumber != null) {
		        	criteria.setFirstResult(pageNumber * pageSize);
		        } else {
		        	criteria.setFirstResult(0);
		        }
		    }
		
		    List<TaxonRelationship> result = (List<TaxonRelationship>)criteria.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    
		    return result;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));
			
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
			
			List<TaxonRelationship> result = (List<TaxonRelationship>)query.getResultList();
			defaultBeanInitializer.initializeAll(result, propertyPaths);
			
			// Ugly, but for now, there is no way to sort on a related entity property in Envers,
			// and we can't live without this functionality in CATE as it screws up the whole 
			// taxon tree thing
			if(orderHints != null && !orderHints.isEmpty()) {
			    SortedSet<TaxonRelationship> sortedList = new TreeSet<TaxonRelationship>(new TaxonRelationshipFromTaxonComparator());
			    sortedList.addAll(result);
			    return new ArrayList<TaxonRelationship>(sortedList);
			}
			
			return result;
		}
	}
	
	class TaxonRelationshipFromTaxonComparator implements Comparator<TaxonRelationship> {

		public int compare(TaxonRelationship o1, TaxonRelationship o2) {
			return o1.getFromTaxon().getTitleCache().compareTo(o2.getFromTaxon().getTitleCache());
		}
		
	}

	public List<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(SynonymRelationship.class);
            
			criteria.add(Restrictions.eq("relatedTo", taxon));
		    if(type != null) {
		    	criteria.add(Restrictions.eq("type", type));
		    } 
		
            addOrder(criteria,orderHints);
		
		    if(pageSize != null) {
		    	criteria.setMaxResults(pageSize);
		        if(pageNumber != null) {
		        	criteria.setFirstResult(pageNumber * pageSize);
		        } else {
		        	criteria.setFirstResult(0);
		        }
		    }
		
		    List<SynonymRelationship> result = (List<SynonymRelationship>)criteria.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    
		    return result;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));
			
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
			
			List<SynonymRelationship> result = (List<SynonymRelationship>)query.getResultList();
			defaultBeanInitializer.initializeAll(result, propertyPaths);
			
			return result;
		}
	}

	public List<TaxonBase> search(Class<? extends TaxonBase> clazz, String queryString,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)  {
		checkNotInPriorView("TaxonDaoHibernateImpl.searchTaxa(String queryString, Boolean accepted,	Integer pageSize, Integer pageNumber)");
		QueryParser queryParser = new QueryParser(defaultField, new SimpleAnalyzer());
		List<TaxonBase> results = new ArrayList<TaxonBase>();
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.getFullTextSession(getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = null;
			
			if(clazz == null) {
				fullTextQuery = fullTextSession.createFullTextQuery(query, TaxonBase.class);
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
		    
		    List<TaxonBase> result = (List<TaxonBase>)fullTextQuery.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    return result;

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
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
		
		for(TaxonBase taxonBase : list(null,null)) { // re-index all taxon base
			Hibernate.initialize(taxonBase.getName());
			fullTextSession.index(taxonBase);
		}
		fullTextSession.flushToIndexes();
	}
	
	public void optimizeIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		SearchFactory searchFactory = fullTextSession.getSearchFactory();
		for(Class clazz : indexedClasses) {
	        searchFactory.optimize(clazz); // optimize the indices ()
		}
	    fullTextSession.flushToIndexes();
	}

	public String suggestQuery(String queryString) {
		checkNotInPriorView("TaxonDaoHibernateImpl.suggestQuery(String queryString)");
		String alternativeQueryString = null;
		if (alternativeSpellingSuggestionParser != null) {
			try {

				alternativeSpellingSuggestionParser.parse(queryString);
				org.apache.lucene.search.Query alternativeQuery = alternativeSpellingSuggestionParser.suggest(queryString);
				if (alternativeQuery != null) {
					alternativeQueryString = alternativeQuery
							.toString("name.titleCache");
				}

			} catch (ParseException e) {
				throw new QueryParseException(e, queryString);
			}
		}
		return alternativeQueryString;
	}
}
