/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.reference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.query.Query;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceDefaultCacheStrategy;

/**
 * @author a.mueller
 */
@Repository
@Qualifier("referenceDaoHibernateImpl")
public class ReferenceDaoHibernateImpl
        extends IdentifiableDaoBase<Reference>
        implements IReferenceDao {

    private static final Logger logger = LogManager.getLogger();

	public ReferenceDaoHibernateImpl() {
		super(Reference.class);
	}

	@Override
	public void rebuildIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());

		for(Reference reference : list(null,null)) { // re-index all agents
			Hibernate.initialize(reference.getAuthorship());

			if(reference.getType().equals(ReferenceType.Article)) {
				Hibernate.initialize(reference.getInJournal());
			} else if(reference.getType().equals(ReferenceType.BookSection)) {
				   Hibernate.initialize(reference.getInBook());
			} else if(reference.getType().equals(ReferenceType.InProceedings)) {
					Hibernate.initialize(reference.getInProceedings());
			}else if(reference.getType().equals(ReferenceType.Thesis)) {
				Hibernate.initialize(reference.getSchool());
			} else if(reference.getType().equals(ReferenceType.Report)) {
				Hibernate.initialize(reference.getInstitution());
			} else if(reference.getType().isPrintedUnit()) {
				Hibernate.initialize(reference.getInSeries());
			}
			fullTextSession.index(reference);
		}
		fullTextSession.flushToIndexes();
	}

    @Override
    public List<UuidAndTitleCache<Reference>> getUuidAndTitleCache(Set<UUID> uuids, ReferenceType refType) {
        List<Reference> result = getReferenceListForUuids(uuids, refType);
        List<UuidAndTitleCache<Reference>> list = new ArrayList<>();

        for(Reference ref : result){
                list.add(new UuidAndTitleCache<Reference>(type, ref.getUuid(), ref.getId(), ref.getTitleCache(), ref.getAbbrevTitleCache()));
        }

        return list;
    }

	private List<Reference> getReferenceListForUuids(Set<UUID> uuids, ReferenceType refType){
	    if (uuids.isEmpty()){
            return new ArrayList<>();
        }

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Reference> cq = cb.createQuery(Reference.class);
        Root<Reference> root = cq.from(Reference.class);

        List<Predicate> predicates = new ArrayList<>();
        if (refType != null) {
            predicates.add(cb.equal(root.get("type"), refType));
        }
        predicates.add(root.get("uuid").in(uuids));

        cq.select(root)
          .where(cb.and(predicates.toArray(new Predicate[0])));

        return getSession().createQuery(cq).getResultList();
	}

	@Override
	public List<UuidAndTitleCache<Reference>> getUuidAndTitleCache(Integer limit, String pattern, ReferenceType refType) {
		List<UuidAndTitleCache<Reference>> list = new ArrayList<>();
		Session session = getSession();

		String queryString = "SELECT " +"r.uuid, r.id, r.titleCache, ab.titleCache "
		        + " FROM " + type.getSimpleName() + " AS r LEFT OUTER JOIN r.authorship AS ab ";

		if (refType != null || pattern != null){
		    queryString += " WHERE (1=1) ";
		    if (refType != null ){
		        queryString += " AND (r.type = :type) ";// OR r.type = :genericType) " ;
		    }
		    if (pattern != null){
		        queryString += " AND (r.titleCache LIKE :pattern) OR (r.abbrevTitleCache LIKE :pattern) ";
		    }
		}

		Query<Object[]> query = session.createQuery(queryString, Object[].class);

		if (limit != null){
		    query.setMaxResults(limit);
		}
		if (pattern != null){
		      pattern = pattern.replace("*", "%");
		      pattern = pattern.replace("?", "_");
		      pattern = pattern + "%";
	          query.setParameter("pattern", pattern);
	    }
		if (refType != null){
		    query.setParameter("type", refType);
		   // query.setParameter("genericType", ReferenceType.Generic);
		}

        List<Object[]> result = query.list();

		for(Object[] object : result){
			String referenceTitle = (String) object[2];

			if(referenceTitle != null){
				String teamTitle = (String) object[3];
				referenceTitle = ReferenceDefaultCacheStrategy.putAuthorToEndOfString(referenceTitle, teamTitle);

				list.add(new UuidAndTitleCache<Reference>(Reference.class, (UUID) object[0],(Integer)object[1], referenceTitle));
			}else{
				logger.warn("Title cache of reference is null. This should not happen. Please fix data. UUID: " + object[0]);
			}
		}

		return list;
	}

	@Override
	public List<Object[]> findByIdentifierAbbrev(String identifier, DefinedTermBase identifierType,
            MatchMode matchmode,Integer limit){
	    checkNotInPriorView("IdentifiableDaoBase.findByIdentifier(T clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");


        String queryString = "SELECT ids.type, ids.identifier, %s FROM %s as c " +
                " INNER JOIN c.identifiers as ids " +
                " WHERE (1=1) ";
        queryString = String.format(queryString, "c.uuid, c.titleCache, c.abbrevTitleCache" , "Reference");

        //Matchmode and identifier
        if (identifier != null){
            if (matchmode == null || matchmode == MatchMode.EXACT){
                queryString += " AND ids.identifier = '"  + identifier + "'";
            }else {
                queryString += " AND ids.identifier LIKE '" + matchmode.queryStringFrom(identifier)  + "'";
            }
        }
        if (identifierType != null){
            queryString += " AND ids.type = :type";
        }
        //order
        queryString +=" ORDER BY ids.type.uuid, ids.identifier, c.uuid ";

        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);

        //parameters
        if (identifierType != null){
            query.setParameter("type", identifierType);
        }

        List<Object[]> results = query.list();
        //initialize

        return results;
	}

	@Override
    public List<Reference> getAllReferencesForPublishing(){
		@SuppressWarnings("unchecked")
        List<Reference> references = getSession().createQuery("SELECT r FROM Reference r "+
				"WHERE r.id IN "+
					"(SELECT m.markedObj.id FROM Marker m WHERE "+
						"m.markerType.id = "+
							"(SELECT dtb.id FROM DefinedTermBase dtb, Representation r WHERE r MEMBER OF dtb.representations AND r.text='publish'))").list();
		return references;
	}

	@Override
    public List<Reference> getAllNotNomenclaturalReferencesForPublishing(){

		@SuppressWarnings("unchecked")
        List<Reference> references = getSession().createQuery("select t.nomenclaturalReference from TaxonName t").list();
		String queryString = "from Reference b where b not in (:referenceList) and b in (:publish)" ;
		Query<Reference> referenceQuery = getSession()
		        .createQuery(queryString, Reference.class)
		        .setParameterList("referenceList", references)
		        .setParameterList("publish", getAllReferencesForPublishing());

        return referenceQuery.list();
	}

	// the result list held doubles therefore I put a "distinct" in the query string
	@Override
    public List<Reference> getAllNomenclaturalReferences() {

        List<Reference> references = getSession().createQuery(
				  " SELECT DISTINCT ns.citation "
				+ " FROM TaxonName n"
				+ " JOIN n.nomenclaturalSource ns ", Reference.class)
                .list();
		return references;
	}


    @Override
	public List<Reference> getSubordinateReferences(Reference reference) {

		List<Reference> references = new ArrayList<>();
		List<Reference> subordinateReferences = new ArrayList<>();

		Query<Reference> query = getSession().createQuery(
		        "select r from Reference r where r.inReference = (:reference)",
		        Reference.class);
		query.setParameter("reference", reference);

	    List<Reference> list = query.list();
	    references.addAll(list);
		for(Reference ref : references){
			subordinateReferences.addAll(getSubordinateReferences(ref));
		}
		references.addAll(subordinateReferences);
		return references;
	}

    @Override
	public List<TaxonBase> listCoveredTaxa(Reference reference, boolean includeSubordinateReferences,
	        List<OrderHint> orderHints, List<String> propertyPaths) {

		/*
		 * <li>taxon.name.nomenclaturalSource.citation</li>
		 * <li>taxon.descriptions.descriptionElement.sources.citation</li>
		 * <li>taxon.descriptions.descriptionSources</li>
		 * <li>taxon.name.descriptions.descriptionElement.sources</li>
		 * <li>taxon.name.descriptions.descriptionSources</li>
		 */

		//TODO implement search in nameDescriptions
		Set<Reference> referenceSet = new HashSet<>();
		referenceSet.add(reference);
		if(includeSubordinateReferences){
			referenceSet.addAll(getSubordinateReferences(reference));
		}

		StringBuilder taxonDescriptionSql = new StringBuilder();
		taxonDescriptionSql.append(
			"SELECT DISTINCT t from Taxon t " +
			// TaxonDescription
			"LEFT JOIN t.descriptions td " +
			"LEFT JOIN td.descriptionSources td_s " +
			"LEFT JOIN td.descriptionElements td_e " +
			"LEFT JOIN td_e.sources td_e_s " +
			// TaxonNameDescription
			"LEFT JOIN t.name n " +
			"LEFT JOIN n.descriptions nd " +
			"LEFT JOIN nd.descriptionSources nd_s " +
			"LEFT JOIN nd.descriptionElements nd_e " +
			"LEFT JOIN nd_e.sources nd_e_s " +
			//nomenclatural citation
			"LEFT JOIN n.nomenclaturalSource ns " +
			//secundum citation
            "LEFT JOIN t.secSource ss " +

			"WHERE td_e_s.citation IN (:referenceBase_1) " +
			  " OR td_s IN (:referenceBase_2) " +
			  " OR nd_e_s.citation IN (:referenceBase_3) " +
			  " OR nd_s IN (:referenceBase_4) " +
			  " OR ns.citation IN (:referenceBase_5) " +
			  " OR ss.citation IN (:referenceBase_6)"
			);

		if (orderHints != null && orderHints.size() > 0){
		    taxonDescriptionSql.append(" order by ");
		    int i = 0;
		    for (OrderHint hint : orderHints) {
		        if(i > 0) {
		            taxonDescriptionSql.append(", ");
		        }
		        taxonDescriptionSql.append("t.").append(hint.toHql());
            }
		}

		// TODO include:
		// name relations
		// taxon relations

		Query<TaxonBase> query = getSession().createQuery(taxonDescriptionSql.toString(), TaxonBase.class);
		query.setParameterList("referenceBase_1", referenceSet);
		query.setParameterList("referenceBase_2", referenceSet);
		query.setParameterList("referenceBase_3", referenceSet);
		query.setParameterList("referenceBase_4", referenceSet);
		query.setParameterList("referenceBase_5", referenceSet);
		query.setParameterList("referenceBase_6", referenceSet);

        List<TaxonBase> taxonBaseList = query.list();

		defaultBeanInitializer.initializeAll(taxonBaseList, propertyPaths);

		return taxonBaseList;
	}

    @Override
    public List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCache(Integer limit, String pattern, ReferenceType refType) {
        Session session = getSession();

        Query<Object[]> query = null;
        if (pattern != null){
            if (pattern.startsWith("*")){
                //requirement probably by WGB to allow search also on titleCache if search starts with *
                query = session.createQuery("select uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName() +" where abbrevTitleCache like :pattern OR titleCache like :pattern ", Object[].class);
            }else{
                query = session.createQuery("select uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName() +" where abbrevTitleCache like :pattern ", Object[].class);
            }
            pattern = pattern + "%";
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery("select uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName(), Object[].class);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }

        return getUuidAndAbbrevTitleCache(query);
    }

    /**
     * @deprecated not used, may be removed in future
     */
    @Deprecated
    @Override
    public List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCacheForAuthor(Integer limit, String authorPattern, ReferenceType refType) {
        Session session = getSession();

        Query<Object[]> query = null;
        if (authorPattern != null){
            query = session.createQuery("SELECT uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName()
            +" as r where r.authorship.nomenclaturalTitleCache like :pattern  ", Object[].class);

            query.setParameter("pattern", authorPattern);
        } else {
            query = session.createQuery("select uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName(), Object[].class);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }
        if(authorPattern != null){
            authorPattern = authorPattern.replace("*", "%");
            authorPattern = authorPattern.replace("?", "_");
            query.setParameter("pattern", authorPattern);
        }
        return getUuidAndAbbrevTitleCache(query);
    }

    @Override
    public List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCacheForAuthorID(Integer limit, Integer authorID, ReferenceType refType) {
        Session session = getSession();

        Query<Object[]> query = null;
        if (authorID != null){
            query = session.createQuery("SELECT uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName()
            +" as r where r.authorship.id = :authorID  ",
            Object[].class);

            query.setParameter("authorID", authorID);
        } else {
            query = session.createQuery("select uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName(), Object[].class);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }

        return getUuidAndAbbrevTitleCache(query);
    }

    @Override
    public List<Reference> findByTitleAndAbbrevTitle(Class clazz, String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Set<String> params = new HashSet<>();
        params.add("titleCache");
        params.add("abbrevTitleCache");

        return findByParam(clazz, params, queryString, matchmode, criterion, pageSize, pageNumber, orderHints, propertyPaths);
    }
}