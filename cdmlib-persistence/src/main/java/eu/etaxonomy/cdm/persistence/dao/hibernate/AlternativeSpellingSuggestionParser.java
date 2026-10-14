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
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.IAlternativeSpellingSuggestionParser;

/**
 * @param <T>
 * @deprecated Spelling support is disabled (see spelling.xml). Kept only so existing
 * subclasses still compile after the Hibernate Search 6 / Lucene 8 migration.
 */
@Deprecated
public abstract class AlternativeSpellingSuggestionParser<T extends CdmBase>
		extends HibernateDaoSupport
		implements IAlternativeSpellingSuggestionParser {

    private static final Logger logger = LogManager.getLogger();

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
			TokenStream source = getAnalyzer().tokenStream(field, new StringReader(queryText));
			List<String> terms = new ArrayList<>();
			try {
				CharTermAttribute termAttr = source.addAttribute(CharTermAttribute.class);
				source.reset();
				while (source.incrementToken()) {
					terms.add(termAttr.toString());
				}
				source.end();
			} catch (IOException e) {
				terms.clear();
			} finally {
				try {
					source.close();
				} catch (IOException e) {
					// ignore
				}
			}

			if (terms.isEmpty()) {
                return null;
            } else if (terms.size() == 1) {
                return new TermQuery(getTerm(field, terms.get(0)));
            } else {
				PhraseQuery.Builder builder = new PhraseQuery.Builder();
				builder.setSlop(getPhraseSlop());
				for (String term : terms) {
					builder.add(getTerm(field, term));
				}
				return builder.build();
			}
		}

		private Term getTerm(String field, String queryText) throws ParseException {

			try {
				SpellChecker spellChecker = new SpellChecker(directory);
				if (spellChecker.exist(queryText)) {
				    spellChecker.close();
					return new Term(field, queryText);
				}
				String[] similarWords = spellChecker.suggestSimilar(queryText, 1);
				if (similarWords.length == 0) {
				    spellChecker.close();
					return new Term(field, queryText);
				}
				suggestedQuery = true;
				spellChecker.close();
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
		// Spelling dictionary refresh relied on Hibernate Search 5 IndexReaderAccessor
		// APIs that no longer exist. Spelling support itself is disabled in the app context.
		logger.warn("AlternativeSpellingSuggestionParser.refresh() is a no-op under Hibernate Search 6");
	}

}
