// $Id$
/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.BooleanClause.Occur;
import org.hibernate.Session;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.reader.ReaderProvider;
import org.hibernate.search.store.DirectoryProvider;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * A LuceneSearch which allow to run a union like search on multiple indexes at once.
 * Internally a {@link MultiReader} is being used.
 *
 * @author Andreas Kohlbecker
 * @date Dec 21, 2011
 *
 */
public class LuceneMultiSearch extends LuceneSearch {

    public static final Logger logger = Logger.getLogger(LuceneMultiSearch.class);

    private Set<Class<? extends CdmBase>> directorySelectClasses = new HashSet<Class<? extends CdmBase>>();


    /**
     * @param luceneSearch the searches to execute together as a union like search
     */
    public LuceneMultiSearch(LuceneSearch... luceneSearch) {
        session = luceneSearch[0].session;

        BooleanQuery query = new BooleanQuery();

        Set<String> highlightFields = new HashSet<String>();

        for(LuceneSearch search : luceneSearch){
            this.directorySelectClasses.add(search.getDirectorySelectClass());
            query.add(search.getQuery(), Occur.SHOULD);
            highlightFields.addAll(Arrays.asList(search.getHighlightFields()));
        }

        this.highlightFields = highlightFields.toArray(new String[highlightFields.size()]);
        this.query = query;

    }

    /**
     * @return
     */
    @Override
    public Searcher getSearcher() {

        if(searcher == null){

            SearchFactory searchFactory = Search.getFullTextSession(session).getSearchFactory();
            List<IndexReader> readers = new ArrayList<IndexReader>();
            for(Class<? extends CdmBase> type : directorySelectClasses){
                DirectoryProvider[] directoryProviders = searchFactory.getDirectoryProviders(type);
                logger.info(directoryProviders[0].getDirectory().toString());

                ReaderProvider readerProvider = searchFactory.getReaderProvider();
                readers.add(readerProvider.openReader(directoryProviders[0]));
            }
            if(readers.size() > 1){
                MultiReader multireader = new MultiReader(readers.toArray(new IndexReader[readers.size()]), true);
                searcher = new IndexSearcher(multireader);
            } else {
                searcher = new IndexSearcher(readers.get(0));
            }
        }

        return searcher;
    }

    /**
     * does exactly the same as {@link LuceneSearch#getAnalyzer()} but perform
     * an additional check to assure that all indexes are using the same
     * analyzer
     *
     * @return
     */
    @Override
    public Analyzer getAnalyzer() {
        SearchFactory searchFactory = Search.getFullTextSession(session).getSearchFactory();
        Analyzer analyzer = null;
        for(Class<? extends CdmBase> type : directorySelectClasses){
            Analyzer a = searchFactory.getAnalyzer(type);
            if(isEqual(analyzer, a)){
                throw new RuntimeException("The LuceneMultiSearch must only be used on indexes which are using the same Analyzer.");
            }
            analyzer = a;
        }
        return analyzer;
    }

    /**
     * @param analyzer
     * @param a
     * @return
     */
    private boolean isEqual(Analyzer analyzer, Analyzer a) {
        // FIXME PatternAnalyzers must be compared by Pattern also other analyzers must be compared by their properties
        return analyzer != null && !analyzer.getClass().equals(a.getClass());
    }

}
