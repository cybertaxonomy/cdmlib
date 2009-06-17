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

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.Hibernate;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.BibtexReference;
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
import eu.etaxonomy.cdm.model.reference.PublicationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.Report;
import eu.etaxonomy.cdm.model.reference.SectionBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.reference.Thesis;
import eu.etaxonomy.cdm.model.reference.WebPage;
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
		indexedClasses = new Class[16];
		indexedClasses[0] = Article.class;
		indexedClasses[1] = BibtexReference.class;
		indexedClasses[2] = Patent.class;
		indexedClasses[3] = PersonalCommunication.class;
		indexedClasses[4] = BookSection.class;
		indexedClasses[5] = InProceedings.class;
		indexedClasses[6] = CdDvd.class;
		indexedClasses[7] = Database.class;
		indexedClasses[8] = Generic.class;
		indexedClasses[9] = Journal.class;
		indexedClasses[10] = Map.class;
		indexedClasses[11] = WebPage.class;
		indexedClasses[12] = Book.class;
		indexedClasses[13] = Proceedings.class;
		indexedClasses[14] = Report.class;
		indexedClasses[15] = Thesis.class;
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
			
			if(reference instanceof StrictReferenceBase) {
				if(reference instanceof Article) {
					Hibernate.initialize(((Article)reference).getInJournal());
				} else if(reference instanceof SectionBase) {
					if(reference instanceof BookSection) {
					    Hibernate.initialize(((BookSection)reference).getInBook());
					} else if(reference instanceof InProceedings) {
						Hibernate.initialize(((InProceedings)reference).getInProceedings());
					}
				} else if(reference instanceof PublicationBase) {
					if(reference instanceof Thesis) {
						Hibernate.initialize(((Thesis)reference).getSchool());
					} else if(reference instanceof Report) {
						Hibernate.initialize(((Report)reference).getInstitution());
					} else if(reference instanceof PrintedUnitBase) {
						Hibernate.initialize(((PrintedUnitBase)reference).getInSeries());
					}
				}
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

	
}