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
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.indexes.IndexReaderAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.IAlternativeSpellingSuggestionParser;


/**
 * @author unknown
 *
 * @param <T>
 * @deprecated Use current methods for alternative spelling suggestions. This class is no longer supported
 * after migration to hibernate 4.x.
 */
@Deprecated
public abstract class AlternativeSpellingSuggestionParser<T extends CdmBase>
		extends HibernateDaoSupport
		implements IAlternativeSpellingSuggestionParser {
	private static Log log = LogFactory.getLog(AlternativeSpellingSuggestionParser.class);

	private String defaultField;
	protected Directory directory;
	private final Class<T> type;
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

	@Override
    public Query parse(String queryString) throws ParseException {
		QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());
		return queryParser.parse(queryString);
	}

	@Override
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
		@Override
        protected Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
			// Copied from org.apache.lucene.queryParser.QueryParser
			// replacing construction of TermQuery with call to getTermQuery()
			// which finds close matches.
			TokenStream source;
            source = getAnalyzer().tokenStream(field, new StringReader(queryText));
			Vector<Object> v = new Vector<Object>();
			Token t;

			while (true) {
				try {
					//OLD
//					t = source.next();

					//FIXME this is new after Hibernate 4 migration
					//but completely unchecked and unsure if correct
					//#3344
					boolean it = source.incrementToken();
					t = source.getAttribute(Token.class);



				} catch (IOException e) {
					t = null;
				}
				if (t == null){
					break;
				}

//		OLD		v.addElement(t.termText());
				//FIXME unchecked #3344
				//FIXME #4716  not sure if this implementation equals the old t.term()
                String term = new String(t.buffer(), 0, t.length());

				v.addElement(term);
			}
			try {
				source.close();
			} catch (IOException e) {
				// ignore
			}

			if (v.size() == 0) {
                return null;
            } else if (v.size() == 1) {
                return new TermQuery(getTerm(field, (String) v.elementAt(0)));
            } else {
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

	@Override
    public void refresh() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		SearchFactory searchFactory = fullTextSession.getSearchFactory();
		try {
			SpellChecker spellChecker = new SpellChecker(directory);

			for(Class<? extends T> indexedClass : indexedClasses) {
				//OLD
//				DirectoryProvider<?> directoryProvider = searchFactory.getDirectoryProviders(indexedClass)[0];
//				ReaderProvider readerProvider = searchFactory.getReaderProvider();
				IndexReaderAccessor ira = searchFactory.getIndexReaderAccessor();
//				IndexReader indexReader = ira.open(indexedClass);
				IndexReader indexReader = null;

				try {

					indexReader = ira.open(indexedClass);
//					indexReader = readerProvider.openIndexReader(); //  .openReader(directoryProvider);
					log.debug("Creating new dictionary for words in " + defaultField + " docs " + indexReader.numDocs());

					Dictionary dictionary = new LuceneDictionary(indexReader, defaultField);
					if(log.isDebugEnabled()) {
						BytesRefIterator iterator = dictionary.getEntryIterator();
						BytesRef bytesRef;
						while((bytesRef = iterator.next())  != null) {
							log.debug("Indexing word " + bytesRef);
						}
					}


//					OLD: spellChecker.indexDictionary(dictionary);
					//FIXME preliminary for Hibernate 4 migration see # 3344
					IndexWriterConfig config = new IndexWriterConfig( new StandardAnalyzer());
					boolean fullMerge = true;
					spellChecker.indexDictionary(dictionary, config, fullMerge);

				} catch (CorruptIndexException cie) {
					log.error("Spellings index is corrupted", cie);
				} finally {
					if (indexReader != null) {
//						readerProvider.closeIndexReader(indexReader);
						ira.close(indexReader);
					}
				}
			}
		}catch (IOException ioe) {
			log.error(ioe);
		}
	}

}
