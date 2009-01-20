package eu.etaxonomy.cdm.persistence.dao;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

public interface IAlternativeSpellingSuggestionParser {
	public Query parse(String queryString) throws ParseException;
	public Query suggest(String queryString) throws ParseException;
	
	public void refresh();
}
