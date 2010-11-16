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

import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IInProceedings;
import eu.etaxonomy.cdm.model.reference.IPrintedUnitBase;
import eu.etaxonomy.cdm.model.reference.IReport;
import eu.etaxonomy.cdm.model.reference.IThesis;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceBaseDefaultCacheStrategy;

/**
 * @author a.mueller
 *
 */
@Repository
@Qualifier("referenceDaoHibernateImpl")
public class ReferenceDaoHibernateImpl extends IdentifiableDaoBase<ReferenceBase> implements IReferenceDao {
	private static final Logger logger = Logger.getLogger(ReferenceDaoHibernateImpl.class);

	public ReferenceDaoHibernateImpl() {
		super(ReferenceBase.class);
	}

	@Override
	public void rebuildIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		
		for(ReferenceBase reference : list(null,null)) { // re-index all agents
			Hibernate.initialize(reference.getAuthorTeam());
			
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
			} else if(reference.getType().equals(ReferenceType.PrintedUnitBase)) {
				Hibernate.initialize(((IPrintedUnitBase)reference).getInSeries());
			}
			fullTextSession.index(reference);
		}
		fullTextSession.flushToIndexes();
	}

	public List<UuidAndTitleCache<ReferenceBase>> getUuidAndTitle(){
		List<UuidAndTitleCache<ReferenceBase>> list = new ArrayList<UuidAndTitleCache<ReferenceBase>>();
		Session session = getSession();
		
		Query query = session.createQuery("select uuid, titleCache from " + type.getSimpleName());
		
		List<Object[]> result = query.list();
		
		for(Object[] object : result){
			list.add(new UuidAndTitleCache<ReferenceBase>(type, (UUID) object[0], (String) object[1]));
		}
		
		return list;
	}
	
	@Override
	public List<UuidAndTitleCache<ReferenceBase>> getUuidAndTitleCache() {
		List<UuidAndTitleCache<ReferenceBase>> list = new ArrayList<UuidAndTitleCache<ReferenceBase>>();
		Session session = getSession();
		
		Query query = session.createQuery("select " +
				"r.uuid, r.titleCache, ab.titleCache from " + type.getSimpleName() + " as r left outer join r.authorTeam as ab ");//"select uuid, titleCache from " + type.getSimpleName());
		
		List<Object[]> result = query.list();
		
		for(Object[] object : result){
			UuidAndTitleCache<ReferenceBase> uuidAndTitleCache;
			String referenceTitle = (String) object[1];
			
			if(referenceTitle != null){							
				String teamTitle = (String) object[2];
				referenceTitle = ReferenceBaseDefaultCacheStrategy.putAuthorToEndOfString(referenceTitle, teamTitle);
				
				list.add(new UuidAndTitleCache<ReferenceBase>(ReferenceBase.class, (UUID) object[0], referenceTitle));
			}else{
				logger.error("title cache of reference is null. UUID: " + object[0]);
			}
		}
		
		return list;
	}
	
	public List<ReferenceBase> getAllReferencesForPublishing(){
		List<ReferenceBase> references = getSession().createQuery("Select r from ReferenceBase r "+
				"where r.id IN "+
					"(Select m.markedObj.id from Marker m where "+
						"m.markerType.id = "+
							"(Select dtb.id from DefinedTermBase dtb, Representation r where r member of dtb.representations and r.text='publish'))").list();
		return references;
	}
	
	public List<ReferenceBase> getAllNotNomenclaturalReferencesForPublishing(){
		
		List<ReferenceBase> references = getSession().createQuery("select t.nomenclaturalReference from TaxonNameBase t").list();
		String queryString = "from ReferenceBase b where b not in (:referenceList) and b in (:publish)" ;
		Query referenceQuery = getSession().createQuery(queryString).setParameterList("referenceList", references);
		referenceQuery.setParameterList("publish", getAllReferencesForPublishing());
		List<ReferenceBase> resultRefernces =referenceQuery.list();
				
		return resultRefernces;
	}
	
	public List<ReferenceBase> getAllNomenclaturalReferences() {
		List<ReferenceBase> references = getSession().createQuery(
				"select t.nomenclaturalReference from TaxonNameBase t").list();
		return references;
	}

	@Override
	public List<ReferenceBase> getSubordinateReferences(
			ReferenceBase referenceBase) {
		
		List<ReferenceBase> references = new ArrayList();
		List<ReferenceBase> subordinateReferences = new ArrayList();
		
		Query query = getSession().createQuery("select r from ReferenceBase r where r.inReference = (:reference)");
		query.setParameter("reference", referenceBase);
		references.addAll(query.list());
		for(ReferenceBase ref : references){
			subordinateReferences.addAll(getSubordinateReferences(ref));
		}
		references.addAll(subordinateReferences);
		return references;
	}

	@Override
	public List<TaxonBase> listCoveredTaxa(ReferenceBase referenceBase, boolean includeSubordinateReferences, List<String> propertyPaths) {
		
		/*
		 * <li>taxon.name.nomenclaturalreference</li>
		 * <li>taxon.descriptions.descriptionElement.sources.citation</li>
		 * <li>taxon.descriptions.descriptionSources</li>
		 * <li>taxon.name.descriptions.descriptionElement.sources</li>
		 * <li>taxon.name.descriptions.descriptionSources</li>
		 */
		
		//TODO implement search in nameDescriptions
		List<TaxonBase> taxonBaseList = new ArrayList<TaxonBase>();
		Set<ReferenceBase> referenceSet = new HashSet<ReferenceBase>();
		referenceSet.add(referenceBase);
		if(includeSubordinateReferences){
			referenceSet.addAll(getSubordinateReferences(referenceBase));
		}

	
		String taxonDescriptionSql = 
			
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
			;
		
			// TODO include:
			// name relations
			// taxon relations
			
		Query query2 = getSession().createQuery(taxonDescriptionSql);
		query2.setParameterList("referenceBase_1", referenceSet);
		query2.setParameterList("referenceBase_2", referenceSet);
		query2.setParameterList("referenceBase_3", referenceSet);
		query2.setParameterList("referenceBase_4", referenceSet);
		query2.setParameterList("referenceBase_5", referenceSet);
		query2.setParameterList("referenceBase_6", referenceSet);
		
		taxonBaseList = query2.list();
		
		defaultBeanInitializer.initializeAll(taxonBaseList, propertyPaths);
		
		return taxonBaseList;
	}
}