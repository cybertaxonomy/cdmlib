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

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IInProceedings;
import eu.etaxonomy.cdm.model.reference.IPrintedUnitBase;
import eu.etaxonomy.cdm.model.reference.IReport;
import eu.etaxonomy.cdm.model.reference.IThesis;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.reference.DefaultReferenceCacheStrategy;

/**
 * @author a.mueller
 *
 */
@Repository
@Qualifier("referenceDaoHibernateImpl")
public class ReferenceDaoHibernateImpl extends IdentifiableDaoBase<Reference> implements IReferenceDao {
	private static final Logger logger = Logger.getLogger(ReferenceDaoHibernateImpl.class);

	public ReferenceDaoHibernateImpl() {
		super(Reference.class);
	}

	@Override
	public void rebuildIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());

		for(Reference reference : list(null,null)) { // re-index all agents
			Hibernate.initialize(reference.getAuthorship());

			if(reference.getType().equals(ReferenceType.Article)) {
				Hibernate.initialize(((IArticle)reference).getInJournal());
			} else if(reference.getType().equals(ReferenceType.BookSection)) {
				   Hibernate.initialize(((IBookSection)reference).getInBook());
			} else if(reference.getType().equals(ReferenceType.InProceedings)) {
					Hibernate.initialize(((IInProceedings)reference).getInProceedings());
			}else if(reference.getType().equals(ReferenceType.Thesis)) {
				Hibernate.initialize(((IThesis)reference).getSchool());
			} else if(reference.getType().equals(ReferenceType.Report)) {
				Hibernate.initialize(((IReport)reference).getInstitution());
			} else if(reference.getType().isPrintedUnit()) {
				Hibernate.initialize(((IPrintedUnitBase)reference).getInSeries());
			}
			fullTextSession.index(reference);
		}
		fullTextSession.flushToIndexes();
	}

	@Override
    public List<UuidAndTitleCache<Reference>> getUuidAndTitle(){
		List<UuidAndTitleCache<Reference>> list = new ArrayList<UuidAndTitleCache<Reference>>();
		Session session = getSession();

		Query query = session.createQuery("select uuid, id, titleCache from " + type.getSimpleName());

		@SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

		for(Object[] object : result){
			list.add(new UuidAndTitleCache<Reference>(type, (UUID) object[0], (Integer)object[1], (String) object[2]));
		}

		return list;
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
		        queryString += " AND (r.type = :type OR r.type = :genericType) " ;
		    }
		    if (pattern != null){
		        queryString += " AND (r.titleCache LIKE :pattern) ";
		    }
		}

		Query query;
		//if (pattern != null){
		    query = session.createQuery(queryString);
//		}else{
//		    query = session.createQuery("SELECT " +"r.uuid, r.id, r.titleCache, ab.titleCache FROM " + type.getSimpleName() + " AS r LEFT OUTER JOIN r.authorship AS ab ");//"select uuid, titleCache from " + type.getSimpleName());
//		}

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
		    query.setParameter("genericType", ReferenceType.Generic);
		}
		@SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

		for(Object[] object : result){
			String referenceTitle = (String) object[2];

			if(referenceTitle != null){
				String teamTitle = (String) object[3];
				referenceTitle = DefaultReferenceCacheStrategy.putAuthorToEndOfString(referenceTitle, teamTitle);

				list.add(new UuidAndTitleCache<Reference>(Reference.class, (UUID) object[0],(Integer)object[1], referenceTitle));
			}else{
				logger.error("title cache of reference is null. UUID: " + object[0]);
			}
		}

		return list;
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
        List<Reference> references = getSession().createQuery("select t.nomenclaturalReference from TaxonNameBase t").list();
		String queryString = "from Reference b where b not in (:referenceList) and b in (:publish)" ;
		Query referenceQuery = getSession().createQuery(queryString).setParameterList("referenceList", references);
		referenceQuery.setParameterList("publish", getAllReferencesForPublishing());
		@SuppressWarnings("unchecked")
        List<Reference> resultRefernces =referenceQuery.list();

		return resultRefernces;
	}

	// the result list held doubles therefore I put a "distinct" in the query string
	@Override
    public List<Reference> getAllNomenclaturalReferences() {
		@SuppressWarnings("unchecked")
        List<Reference> references = getSession().createQuery(
				"SELECT DISTINCT t.nomenclaturalReference FROM TaxonNameBase t").list();
		return references;
	}


    @Override
	public List<Reference> getSubordinateReferences(Reference reference) {

		List<Reference> references = new ArrayList();
		List<Reference> subordinateReferences = new ArrayList<Reference>();

		Query query = getSession().createQuery("select r from Reference r where r.inReference = (:reference)");
		query.setParameter("reference", reference);

		@SuppressWarnings("unchecked")
	    List<Reference> list = query.list();
	    references.addAll(list);
		for(Reference ref : references){
			subordinateReferences.addAll(getSubordinateReferences(ref));
		}
		references.addAll(subordinateReferences);
		return references;
	}

    @Override
	public List<TaxonBase> listCoveredTaxa(Reference reference, boolean includeSubordinateReferences, List<OrderHint> orderHints, List<String> propertyPaths) {

		/*
		 * <li>taxon.name.nomenclaturalreference</li>
		 * <li>taxon.descriptions.descriptionElement.sources.citation</li>
		 * <li>taxon.descriptions.descriptionSources</li>
		 * <li>taxon.name.descriptions.descriptionElement.sources</li>
		 * <li>taxon.name.descriptions.descriptionSources</li>
		 */

		//TODO implement search in nameDescriptions
		Set<Reference> referenceSet = new HashSet<Reference>();
		referenceSet.add(reference);
		if(includeSubordinateReferences){
			referenceSet.addAll(getSubordinateReferences(reference));
		}


		StringBuilder taxonDescriptionSql = new StringBuilder();
		taxonDescriptionSql.append(
			"select distinct t from Taxon t " +
			// TaxonDescription
			"left join t.descriptions td " +
			"left join td.descriptionSources td_s " +
			"left join td.descriptionElements td_e " +
			"left join td_e.sources td_e_s " +
			// TaxonNameDescription
			"left join t.name n " +
			"left join n.descriptions nd " +
			"left join nd.descriptionSources nd_s " +
			"left join nd.descriptionElements nd_e " +
			"left join nd_e.sources nd_e_s " +

			"where td_e_s.citation in (:referenceBase_1) " +
			"or td_s in (:referenceBase_2) " +
			"or nd_e_s.citation in (:referenceBase_3) " +
			"or nd_s in (:referenceBase_4) or " +
			"n.nomenclaturalReference in (:referenceBase_5) or " +
			"t.sec in (:referenceBase_6)"
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

		Query query = getSession().createQuery(taxonDescriptionSql.toString());
		query.setParameterList("referenceBase_1", referenceSet);
		query.setParameterList("referenceBase_2", referenceSet);
		query.setParameterList("referenceBase_3", referenceSet);
		query.setParameterList("referenceBase_4", referenceSet);
		query.setParameterList("referenceBase_5", referenceSet);
		query.setParameterList("referenceBase_6", referenceSet);

		@SuppressWarnings("unchecked")
        List<TaxonBase> taxonBaseList = query.list();

		defaultBeanInitializer.initializeAll(taxonBaseList, propertyPaths);

		return taxonBaseList;
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao#getUuidAndAbbrevTitleCache(java.lang.Integer, java.lang.String)
     */
    @Override
    public List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCache(Integer limit, String pattern, ReferenceType refType) {
        Session session = getSession();
        Reference ref = ReferenceFactory.newArticle();

        Query query = null;
        if (pattern != null){
            if (pattern.startsWith("*")){
                query = session.createQuery("select uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName() +" where abbrevTitleCache like :pattern OR titleCache like :pattern ");
            }else{
                query = session.createQuery("select uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName() +" where abbrevTitleCache like :pattern  ");
            }
            pattern = pattern + "%";
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery("select uuid, id, abbrevTitleCache, titleCache from " + type.getSimpleName() );
        }
        if (limit != null){
           query.setMaxResults(limit);
        }

        return getUuidAndAbbrevTitleCache(query);

    }
}
