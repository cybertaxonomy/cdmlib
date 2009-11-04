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
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
/*import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.CdDvd;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.InProceedings;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.Map;
import eu.etaxonomy.cdm.model.reference.Patent;
import eu.etaxonomy.cdm.model.reference.PersonalCommunication;
import eu.etaxonomy.cdm.model.reference.PrintedUnitBase;
import eu.etaxonomy.cdm.model.reference.Proceedings;
import eu.etaxonomy.cdm.model.reference.PublicationBase;*/
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IInProceedings;
import eu.etaxonomy.cdm.model.reference.IPrintedUnitBase;
import eu.etaxonomy.cdm.model.reference.IReport;
import eu.etaxonomy.cdm.model.reference.IThesis;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
/*import eu.etaxonomy.cdm.model.reference.Report;
import eu.etaxonomy.cdm.model.reference.SectionBase;
import eu.etaxonomy.cdm.model.reference.Thesis;
import eu.etaxonomy.cdm.model.reference.WebPage;*/
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 *
 */
@Repository
@Qualifier("referenceDaoHibernateImpl")
public class ReferenceDaoHibernateImpl extends IdentifiableDaoBase<ReferenceBase> implements IReferenceDao {
	
	private String defaultField = "titleCache";
	private Class<? extends ReferenceBase> indexedClasses[]; 
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReferenceDaoHibernateImpl.class);

	public ReferenceDaoHibernateImpl() {
		super(ReferenceBase.class);
		/*indexedClasses = new Class[15];
		indexedClasses[0] = Article.class;
		indexedClasses[1] = Patent.class;
		indexedClasses[2] = PersonalCommunication.class;
		indexedClasses[3] = BookSection.class;
		indexedClasses[4] = InProceedings.class;
		indexedClasses[5] = CdDvd.class;
		indexedClasses[6] = Database.class;
		indexedClasses[7] = Generic.class;
		indexedClasses[8] = Journal.class;
		indexedClasses[9] = Map.class;
		indexedClasses[10] = WebPage.class;
		indexedClasses[11] = Book.class;
		indexedClasses[12] = Proceedings.class;
		indexedClasses[13] = Report.class;
		indexedClasses[14] = Thesis.class;*/
	}

	public int count(Class<? extends ReferenceBase> clazz, String queryString) {
		checkNotInPriorView("ReferenceDaoHibernateImpl.count(String queryString, Boolean accepted)");
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

	public List<ReferenceBase> search(Class<? extends ReferenceBase> clazz,	String queryString, Integer pageSize, Integer pageNumber,List<OrderHint> orderHints, List<String> propertyPaths) {
		checkNotInPriorView("ReferenceDaoHibernateImpl.searchTaxa(String queryString, Boolean accepted,	Integer pageSize, Integer pageNumber)");
		QueryParser queryParser = new QueryParser(defaultField, new SimpleAnalyzer());
		List<ReferenceBase> results = new ArrayList<ReferenceBase>();
		 
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
		    
		    List<ReferenceBase> result = (List<ReferenceBase>)fullTextQuery.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    return result;

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}

	public String suggestQuery(String string) {
		throw new UnsupportedOperationException("suggestQuery is not supported for ReferenceBase");
	}

	public List<UuidAndTitleCache<ReferenceBase>> getUuidAndTitle(){
		List<UuidAndTitleCache<ReferenceBase>> list = new ArrayList<UuidAndTitleCache<ReferenceBase>>();
		Session session = getSession();
		
		Query query = session.createQuery("select uuid, title from " + type.getSimpleName());
		
		List<Object[]> result = query.list();
		
		for(Object[] object : result){
			list.add(new UuidAndTitleCache<ReferenceBase>(type, (UUID) object[0], (String) object[1]));
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
public List<ReferenceBase> getAllNomenclaturalReferences(){
		
		List<ReferenceBase> references = getSession().createQuery("select t.nomenclaturalReference from TaxonNameBase t").list();
		return references;
	}
}