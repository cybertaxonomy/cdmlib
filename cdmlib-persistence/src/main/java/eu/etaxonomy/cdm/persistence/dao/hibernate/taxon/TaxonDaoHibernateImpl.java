/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.AlternativeSpellingSuggestionParser;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.mueller
 * @created 24.11.2008
 * @version 1.0
 */
@Repository
@Qualifier("taxonDaoHibernateImpl")
public class TaxonDaoHibernateImpl extends IdentifiableDaoBase<TaxonBase> implements ITaxonDao {	
	private AlternativeSpellingSuggestionParser<TaxonBase> alternativeSpellingSuggestionParser;
	
	private static final Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);

	public TaxonDaoHibernateImpl() {
		super(TaxonBase.class);
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
	public List<Taxon> 
	getRootTaxa(Rank rank, ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications) {
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
		//crit.add(Restrictions.isNull("taxonomicParentCache"));
		
		crit.setFetchMode("name", FetchMode.JOIN);
		crit.createAlias("name", "name");
		
		if (rank != null) {
			crit.add(Restrictions.eq("name.rank", rank));
		}

		if (sec != null){
			crit.add(Restrictions.eq("sec", sec) );
		}

		if (! cdmFetch.includes(CdmFetch.FETCH_CHILDTAXA())){
			logger.warn("no child taxa fetch");
			//TODO overwrite LAZY (SELECT) does not work (bug in hibernate?)
			crit.setFetchMode("relationsToThisTaxon.fromTaxon", FetchMode.LAZY);
		}

		List<Taxon> results = new ArrayList<Taxon>();
		List<Taxon> taxa = crit.list();
		for(Taxon taxon : taxa){
			//childTaxa
			//TODO create restriction instead
			if (onlyWithChildren == false || taxon.hasTaxonomicChildren()){
				if (withMisapplications == true || ! taxon.isMisappliedName()){
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
		if (onlyWithChildren == null){
			onlyWithChildren = true;
		}
		if (withMisapplications == null){
			withMisapplications = true;
		}
		if (cdmFetch == null){
			cdmFetch = CdmFetch.NO_FETCH();
		}


//		String query = "from Taxon root ";
//		query += " where root.taxonomicParentCache is NULL ";
//		if (sec != null){
//		query += " AND root.sec.id = :sec "; 
//		}		
//		Query q = getSession().createQuery(query);
//		if (sec != null){
//		q.setInteger("sec", sec.getId());
//		}


		Criteria crit = getSession().createCriteria(Taxon.class);
		crit.add(Restrictions.isNull("taxonomicParentCache"));
		if (sec != null){
			crit.add(Restrictions.eq("sec", sec) );
		}


		if (! cdmFetch.includes(CdmFetch.FETCH_CHILDTAXA())){
			logger.warn("no child taxa fetch");
			//TODO overwrite LAZY (SELECT) does not work (bug in hibernate?)
			crit.setFetchMode("relationsToThisTaxon.fromTaxon", FetchMode.LAZY);
		}

		List<Taxon> results = new ArrayList<Taxon>();
		List<Taxon> taxa = crit.list();
		for(Taxon taxon : taxa){
			//childTaxa
			//TODO create restriction instead
			if (onlyWithChildren == false || taxon.hasTaxonomicChildren()){
				if (withMisapplications == true || ! taxon.isMisappliedName()){
					results.add(taxon);
				}
			}
		}
		return results;
	}


	public List<TaxonBase> getTaxaByName(String queryString, ReferenceBase sec) {
		
		return getTaxaByName(queryString, true, sec);
	}

	public List<TaxonBase> getTaxaByName(String queryString, Boolean accepted, ReferenceBase sec) {
		
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

		// FIXME: sec restriction caused problems in cich image import: results was empty
		
		if (queryString != null) {
			criteria.add(Restrictions.ilike("name.nameCache", queryString));
		}
		List<TaxonBase> results = criteria.list();
		return results;
	}

	public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode, 
			Boolean accepted, Integer pageSize, Integer pageNumber) {
		
		Criteria criteria = null;
		if (accepted == true) {
			criteria = getSession().createCriteria(Taxon.class);
		} else {
			criteria = getSession().createCriteria(Synonym.class);
		}

		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		String hqlQueryString = matchMode.queryStringFrom(queryString);
		if (matchMode == MatchMode.EXACT) {
			criteria.add(Restrictions.eq("name.nameCache", hqlQueryString));
		} else {
			criteria.add(Restrictions.ilike("name.nameCache", hqlQueryString));
		}
		
		if(pageSize != null) {
			criteria.setMaxResults(pageSize);
			if(pageNumber != null) {
				criteria.setFirstResult(pageNumber * pageSize);
			}
		}

		List<TaxonBase> results = criteria.list();
		return results;
	}
	
	public List<TaxonBase> getAllTaxonBases(Integer pagesize, Integer page) {
		Criteria crit = getSession().createCriteria(TaxonBase.class);
		List<TaxonBase> results = crit.list();
		// TODO add page & pagesize criteria
		return results;
	}

	public List<Synonym> getAllSynonyms(Integer limit, Integer start) {
		Criteria crit = getSession().createCriteria(Synonym.class);
		List<Synonym> results = crit.list();
		return results;
	}

	public List<Taxon> getAllTaxa(Integer limit, Integer start) {
		Criteria crit = getSession().createCriteria(Taxon.class);
		List<Taxon> results = crit.list();
		return results;
	}

	public List<RelationshipBase> getAllRelationships(Integer limit, Integer start) {
		Criteria crit = getSession().createCriteria(RelationshipBase.class);
		List<RelationshipBase> results = crit.list();
		return results;
	}

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
				TaxonRelationship relationToThisTaxon = iterator.next();
				iterator.remove();
				relationToThisTaxon.setFromTaxon(null);
				relationToThisTaxon.setToTaxon(null);
				getSession().delete(relationToThisTaxon);
			}
			
			for (Iterator<TaxonRelationship> iterator = taxon.getRelationsToThisTaxon().iterator(); iterator.hasNext();){
				TaxonRelationship relationFromThisTaxon = iterator.next();
				iterator.remove();
				relationFromThisTaxon.setToTaxon(null);
				relationFromThisTaxon.setFromTaxon(null);
				getSession().delete(relationFromThisTaxon);
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
				taxonDescription.setTaxon(null);
				for (Iterator<DescriptionElementBase> iterDescElem = 
					taxonDescription.getElements().iterator(); iterDescElem.hasNext();) {
					DescriptionElementBase descriptionElement = iterDescElem.next();
					iterDescElem.remove();
					getSession().delete(descriptionElement);
				}
				getSession().delete(taxonDescription);
			}
			
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

		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("persistentTitleCache", matchMode.queryStringFrom(queryString)));
		crit.setProjection(Projections.rowCount());
		int result = ((Integer)crit.list().get(0)).intValue();
		return result;
	}


	public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted, List<Criterion> criteria) {

		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("persistentTitleCache", matchMode.queryStringFrom(queryString)));
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
		Query query = null;
		
		if(type == null) {
			query = getSession().createQuery("select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship.relatedTo = :relatedTo");
		} else {
			query = getSession().createQuery("select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship.relatedTo = :relatedTo and taxonRelationship.type = :type");
			query.setParameter("type",type);
		}
		
		query.setParameter("relatedTo", taxon);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countSynonyms(Taxon taxon, SynonymRelationshipType type) {
        Query query = null;
		
		if(type == null) {
			query = getSession().createQuery("select count(synonymRelationship) from SynonymRelationship synonymRelationship where synonymRelationship.relatedTo = :relatedTo");
		} else {
			query = getSession().createQuery("select count(synonymRelationship) from SynonymRelationship synonymRelationship where synonymRelationship.relatedTo = :relatedTo and synonymRelationship.type = :type");
			query.setParameter("type",type);
		}
		
		query.setParameter("relatedTo", taxon);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countTaxa(String queryString, Boolean accepted) {
        QueryParser queryParser = new QueryParser("name.persistentTitleCache", new SimpleAnalyzer());
		
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.createFullTextSession(this.getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = null;
			
			if(accepted == null) {
				fullTextQuery = fullTextSession.createFullTextQuery(query, TaxonBase.class);
			} else {
				if(accepted) {
					fullTextQuery = fullTextSession.createFullTextQuery(query, Taxon.class);
				} else {
					fullTextQuery = fullTextSession.createFullTextQuery(query, Synonym.class);
				}
			}
			
		    Integer  result = fullTextQuery.getResultSize();
		    return result;

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}
	
	public int countTaxaByName(String queryString, Boolean accepted, ReferenceBase sec) {
		
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

	public int countTaxaByName(Boolean accepted, String genusOrUninomial, String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank) {
        Criteria criteria = null;
		
		if(accepted == null) {
			criteria = getSession().createCriteria(TaxonBase.class);
		} else {
			if(accepted) {
				criteria = getSession().createCriteria(Taxon.class);
			} else {
				criteria = getSession().createCriteria(Synonym.class);
			}
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
    		
    		
    public List<TaxonBase> findTaxaByName(Boolean accepted, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber) {
		Criteria criteria = null;
		
		if(accepted == null) {
			criteria = getSession().createCriteria(TaxonBase.class);
		} else {
			if(accepted) {
				criteria = getSession().createCriteria(Taxon.class);
			} else {
				criteria = getSession().createCriteria(Synonym.class);
			}
		}
		
		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		if(genusOrUninomial != null) {
			criteria.add(Restrictions.eq("name.genusOrUninomial", genusOrUninomial));
		}
		
		if(infraGenericEpithet != null) {
			criteria.add(Restrictions.eq("name.infraGenericEpithet", infraGenericEpithet));
		} else {
			criteria.add(Restrictions.isNull("name.infraGenericEpithet"));
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

	public List<TaxonRelationship> getRelatedTaxa(Taxon taxon,	TaxonRelationshipType type, Integer pageSize, Integer pageNumber) {
        Query query = null;
		
		if(type == null) {
			query = getSession().createQuery("select taxonRelationship from TaxonRelationship taxonRelationship join fetch taxonRelationship.relatedFrom where taxonRelationship.relatedTo = :relatedTo");
		} else {
			query = getSession().createQuery("select taxonRelationship from TaxonRelationship taxonRelationship join fetch taxonRelationship.relatedFrom where taxonRelationship.relatedTo = :relatedTo and taxonRelationship.type = :type");
			query.setParameter("type",type);
		}
		
		query.setParameter("relatedTo", taxon);
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<TaxonRelationship>)query.list();
	}

	public List<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber) {
        Query query = null;
		
		if(type == null) {
			query = getSession().createQuery("select synonymRelationship from SynonymRelationship synonymRelationship join fetch synonymRelationship.relatedFrom where synonymRelationship.relatedTo = :relatedTo");
		} else {
			query = getSession().createQuery("select synonymRelationship from SynonymRelationship synonymRelationship join fetch synonymRelationship.relatedFrom where synonymRelationship.relatedTo = :relatedTo and synonymRelationship.type = :type");
			query.setParameter("type",type);
		}
		
		query.setParameter("relatedTo", taxon);
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<SynonymRelationship>)query.list();
	}

	public List<TaxonBase> searchTaxa(String queryString, Boolean accepted,	Integer pageSize, Integer pageNumber) {
		QueryParser queryParser = new QueryParser("name.persistentTitleCache", new SimpleAnalyzer());
		List<TaxonBase> results = new ArrayList<TaxonBase>();
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.createFullTextSession(getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = null;
			Criteria criteria = null;
			
			if(accepted == null) {
				fullTextQuery = fullTextSession.createFullTextQuery(query, TaxonBase.class);
				criteria =  getSession().createCriteria( TaxonBase.class );
			} else {
				if(accepted) {
					fullTextQuery = fullTextSession.createFullTextQuery(query, Taxon.class);
					criteria =  getSession().createCriteria( Taxon.class );
				} else {
					fullTextQuery = fullTextSession.createFullTextQuery(query, Synonym.class);
					criteria =  getSession().createCriteria( Synonym.class );
				}
			}
			
			org.apache.lucene.search.Sort sort = new Sort(new SortField("name.titleCache_forSort"));
			fullTextQuery.setSort(sort);
			
			criteria.setFetchMode( "name", FetchMode.JOIN );
		    fullTextQuery.setCriteriaQuery(criteria);
		    
		    if(pageSize != null) {
		    	fullTextQuery.setMaxResults(pageSize);
			    if(pageNumber != null) {
			    	fullTextQuery.setFirstResult(pageNumber * pageSize);
			    } else {
			    	fullTextQuery.setFirstResult(0);
			    }
			}
		    
		    return (List<TaxonBase>)fullTextQuery.list();

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}
	
	public void purgeIndex() {
		FullTextSession fullTextSession = Search.createFullTextSession(getSession());
		
		fullTextSession.purgeAll(type); // remove all taxon base from indexes
		// fullTextSession.flushToIndexes() not implemented in 3.0.0.GA
	}

	public void rebuildIndex() {
		FullTextSession fullTextSession = Search.createFullTextSession(getSession());
		
		for(TaxonBase taxonBase : list(null,null)) { // re-index all taxon base
			Hibernate.initialize(taxonBase.getName());
			fullTextSession.index(taxonBase);
		}
		// fullTextSession.flushToIndexes() not implemented in 3.0.0.GA
	}
	
	public void optimizeIndex() {
		FullTextSession fullTextSession = Search.createFullTextSession(getSession());
		SearchFactory searchFactory = fullTextSession.getSearchFactory();
	    searchFactory.optimize(type); // optimize the indices ()
	    // fullTextSession.flushToIndexes() not implemented in 3.0.0.GA
	}

	public String suggestQuery(String queryString) {
		try {
			String alternativeQueryString = null;
			alternativeSpellingSuggestionParser.parse(queryString);
			org.apache.lucene.search.Query alternativeQuery = alternativeSpellingSuggestionParser.suggest(queryString);
			if(alternativeQuery != null) {
				alternativeQueryString = alternativeQuery.toString("name.persistentTitleCache");
			}
			return alternativeQueryString;
		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}
}