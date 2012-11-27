/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.persistence.dao.hibernate;


import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.reader.ReaderProvider;
import org.hibernate.search.store.DirectoryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.IAlternativeSpellingSuggestionParser;


public abstract class AlternativeSpellingSuggestionParser<T extends CdmBase> extends HibernateDaoSupport  implements
IAlternativeSpellingSuggestionParser {
	private static Log log = LogFactory.getLog(AlternativeSpellingSuggestionParser.class);

	private String defaultField;
	protected Directory directory;
	private Class<T> type;
	private Class<? extends T> indexedClasses[];

	public AlternativeSpellingSuggestionParser(Class<T> type) {
		this.type = type;
	}

	public void setIndexedClasses(Class<? extends T> indexedClasses[]) {
		this.indexedClasses = indexedClasses;
	}

	public abstract void setDirectory(Directory directory);

	@Autowired
	public void setHibernateSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	public void setDefaultField(String defaultField) {
		this.defaultField = defaultField;
	}

	public Query parse(String queryString) throws ParseException {
		QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());		
		return queryParser.parse(queryString);
	}

	public Query suggest(String queryString) throws ParseException {
		QuerySuggester querySuggester = new QuerySuggester(defaultField, new StandardAnalyzer());
		Query query = querySuggester.parse(queryString);
		return querySuggester.hasSuggestedQuery() ? query : null;
	}

	private class QuerySuggester extends QueryParser {
		private boolean suggestedQuery = false;
		public QuerySuggester(String field, Analyzer analyzer) {
			super(field, analyzer);
		}
		protected Query getFieldQuery(String field, String queryText) throws ParseException {
			// Copied from org.apache.lucene.queryParser.QueryParser
			// replacing construction of TermQuery with call to getTermQuery()
			// which finds close matches.
			TokenStream source = getAnalyzer().tokenStream(field, new StringReader(queryText));
			Vector v = new Vector();
			Token t;

			while (true) {
				try {
					t = source.next();
				} catch (IOException e) {
					t = null;
				}
				if (t == null)
					break;
				v.addElement(t.termText());
			}
			try {
				source.close();
			} catch (IOException e) {
				// ignore
			}

			if (v.size() == 0)
				return null;
			else if (v.size() == 1)
				return new TermQuery(getTerm(field, (String) v.elementAt(0)));
			else {
				PhraseQuery q = new PhraseQuery();
				q.setSlop(getPhraseSlop());
				for (int i = 0; i < v.size(); i++) {
					q.add(getTerm(field, (String) v.elementAt(i)));
				}
				return q;
			}
		}

		private Term getTerm(String field, String queryText) throws ParseException {

			try {
				SpellChecker spellChecker = new SpellChecker(directory);
				if (spellChecker.exist(queryText)) {
					return new Term(field, queryText);
				}
				String[] similarWords = spellChecker.suggestSimilar(queryText, 1);
				if (similarWords.length == 0) {
					return new Term(field, queryText);
				}			
				suggestedQuery = true;
				return new Term(field, similarWords[0]);
			} catch (IOException e) {
				throw new ParseException(e.getMessage());
			}
		}		
		public boolean hasSuggestedQuery() {
			return suggestedQuery;
		}	
	}

	public void refresh() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		SearchFactory searchFactory = fullTextSession.getSearchFactory();
		try {
			SpellChecker spellChecker = new SpellChecker(directory);

			for(Class<? extends T> indexedClass : indexedClasses) {
				DirectoryProvider directoryProvider = searchFactory.getDirectoryProviders(indexedClass)[0];
				ReaderProvider readerProvider = searchFactory.getReaderProvider();
				IndexReader indexReader = null;

				try {

					indexReader = readerProvider.openReader(directoryProvider);
					log.debug("Creating new dictionary for words in " + defaultField + " docs " + indexReader.numDocs());

					Dictionary dictionary = new LuceneDictionary(indexReader, defaultField);
					if(log.isDebugEnabled()) {
						Iterator iterator = dictionary.getWordsIterator();
						while(iterator.hasNext()) {
							log.debug("Indexing word " + iterator.next());
						}
					}

					spellChecker.indexDictionary(dictionary);
				} catch (CorruptIndexException cie) {
					log.error("Spellings index is corrupted", cie);
				} finally {
					if (indexReader != null) {
						readerProvider.closeReader(indexReader);
					}
				} 
			} 
		}catch (IOException ioe) {
			log.error(ioe);
		}
	}

}
