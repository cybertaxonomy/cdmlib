package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

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
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;

@Repository
public class DescriptionElementDaoImpl extends AnnotatableDaoImpl<DescriptionElementBase> implements IDescriptionElementDao {

	public DescriptionElementDaoImpl() {
		super(DescriptionElementBase.class);
	}

	public int countMedia(DescriptionElementBase descriptionElement) {
		Query query = getSession().createQuery("select count(media) from DescriptionElementBase descriptionElement join descriptionElement.media media where descriptionElement = :descriptionElement");
		query.setParameter("descriptionElement", descriptionElement);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countTextData(String queryString) {
		QueryParser queryParser = new QueryParser("multilanguageText.text", new SimpleAnalyzer());
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.createFullTextSession(getSession());
			org.hibernate.search.FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, TextData.class);
			return  fullTextQuery.getResultSize();
		} catch (ParseException e) {
			throw new QueryParseException(e, queryString);
		}
	}

	public List<Media> getMedia(DescriptionElementBase descriptionElement,	Integer pageSize, Integer pageNumber) {
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
		QueryParser queryParser = new QueryParser("multilanguageText.text", new SimpleAnalyzer());
		 
		try {
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			FullTextSession fullTextSession = Search.createFullTextSession(getSession());
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
		FullTextSession fullTextSession = Search.createFullTextSession(getSession());
		
		fullTextSession.purgeAll(type); // remove all description element base from indexes
		// fullTextSession.flushToIndexes() not implemented in 3.0.0.GA
	}

	public void rebuildIndex() {
		FullTextSession fullTextSession = Search.createFullTextSession(getSession());
		
		for(DescriptionElementBase descriptionElementBase : list(null,null)) { // re-index all descriptionElements
			Hibernate.initialize(descriptionElementBase.getInDescription());
			Hibernate.initialize(descriptionElementBase.getFeature());
			fullTextSession.index(descriptionElementBase);
		}
	}
	
	public void optimizeIndex() {
		FullTextSession fullTextSession = Search.createFullTextSession(getSession());
		SearchFactory searchFactory = fullTextSession.getSearchFactory();
	    searchFactory.optimize(type); // optimize the indices ()
	}

}
