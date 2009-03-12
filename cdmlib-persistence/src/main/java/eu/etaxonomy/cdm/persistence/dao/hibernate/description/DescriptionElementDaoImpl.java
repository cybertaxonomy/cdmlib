package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.common.ISearchableDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;

@Repository
public class DescriptionElementDaoImpl extends AnnotatableDaoImpl<DescriptionElementBase> implements IDescriptionElementDao {

	private String defaultField = "titleCache";
	private String defaultSort = "titleCache_forSort";
	
	private Class<? extends DescriptionElementBase> indexedClasses[]; 

	public DescriptionElementDaoImpl() {
		super(DescriptionElementBase.class);
		indexedClasses = new Class[1];
		indexedClasses[0] = TextData.class;
	}

	public int countMedia(DescriptionElementBase descriptionElement) {
		checkNotInPriorView("DescriptionElementDaoImpl.countMedia(DescriptionElementBase descriptionElement)");
		Query query = getSession().createQuery("select count(media) from DescriptionElementBase descriptionElement join descriptionElement.media media where descriptionElement = :descriptionElement");
		query.setParameter("descriptionElement", descriptionElement);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countTextData(String queryString) {
		checkNotInPriorView("DescriptionElementDaoImpl.countTextData(String queryString)");
		QueryParser queryParser = new QueryParser("multilanguageText.text", new SimpleAnalyzer());
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.getFullTextSession(getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, TextData.class);
			return  fullTextQuery.getResultSize();
		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}

	public List<Media> getMedia(DescriptionElementBase descriptionElement,	Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("DescriptionElementDaoImpl.getMedia(DescriptionElementBase descriptionElement,	Integer pageSize, Integer pageNumber)");
		Query query = getSession().createQuery("select media from DescriptionElementBase descriptionElement join descriptionElement.media media where descriptionElement = :descriptionElement");
		query.setParameter("descriptionElement", descriptionElement);
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<Media>)query.list();
	}

	public List<TextData> searchTextData(String queryString, Integer pageSize,	Integer pageNumber) {
		checkNotInPriorView("DescriptionElementDaoImpl.searchTextData(String queryString, Integer pageSize,	Integer pageNumber)");
		QueryParser queryParser = new QueryParser("multilanguageText.text", new SimpleAnalyzer());
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.getFullTextSession(getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, TextData.class);
			org.apache.lucene.search.Sort sort = new Sort(new SortField("inDescription.titleCache_forSort"));
			fullTextQuery.setSort(sort);
			
			Criteria criteria = getSession().createCriteria(TextData.class);
			criteria.setFetchMode("inDescription",FetchMode.JOIN);
			criteria.setFetchMode("feature", FetchMode.JOIN);
			
			fullTextQuery.setCriteriaQuery(criteria);
			
		    if(pageSize != null) {
		    	fullTextQuery.setMaxResults(pageSize);
			    if(pageNumber != null) {
			    	fullTextQuery.setFirstResult(pageNumber * pageSize);
			    } else {
			    	fullTextQuery.setFirstResult(0);
			    }
			}
		    List<TextData> textData = fullTextQuery.list();
		    
		    for(TextData t : textData) {
		    	Hibernate.initialize(t.getMultilanguageText());
		    	if(t.getInDescription() instanceof TaxonDescription) {
		    		TaxonDescription taxonDescription = (TaxonDescription)t.getInDescription();
		    		Hibernate.initialize(taxonDescription.getTaxon());
		    	}
		    }
		    return textData;

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}
	
	public void purgeIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		for(Class clazz : indexedClasses) {
		  fullTextSession.purgeAll(type); // remove all description element base from indexes
		}
		fullTextSession.flushToIndexes();
	}

	public void rebuildIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		
		for(DescriptionElementBase descriptionElementBase : list(null,null)) { // re-index all descriptionElements
			Hibernate.initialize(descriptionElementBase.getInDescription());
			Hibernate.initialize(descriptionElementBase.getFeature());
			fullTextSession.index(descriptionElementBase);
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

	public int count(String queryString) {
		checkNotInPriorView("DescriptionElementDaoImpl.count(String queryString)");
        QueryParser queryParser = new QueryParser(defaultField, new SimpleAnalyzer());
		
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
		
			FullTextSession fullTextSession = Search.getFullTextSession(this.getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, type);
				
		    return fullTextQuery.getResultSize();

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}

	public List<DescriptionElementBase> search(String queryString,	Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("DescriptionElementDaoImpl.search(String queryString, Integer pageSize,	Integer pageNumber)");
		QueryParser queryParser = new QueryParser(defaultField, new SimpleAnalyzer());
		List<TaxonBase> results = new ArrayList<TaxonBase>();
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.getFullTextSession(getSession());
			
			org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, type);
			
			org.apache.lucene.search.Sort sort = new Sort(new SortField(defaultSort));
			fullTextQuery.setSort(sort);
		    
		    if(pageSize != null) {
		    	fullTextQuery.setMaxResults(pageSize);
			    if(pageNumber != null) {
			    	fullTextQuery.setFirstResult(pageNumber * pageSize);
			    } else {
			    	fullTextQuery.setFirstResult(0);
			    }
			}
		    
		    return (List<DescriptionElementBase>)fullTextQuery.list();

		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}

	public String suggestQuery(String string) {
		throw new UnsupportedOperationException("suggest query is not supported yet");
	}

}
