/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SortField;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * A LuceneSearch which allow to run a union like search on multiple indexes at once.
 * Internally a {@link MultiReader} is being used.
 *
 * @author Andreas Kohlbecker
 * @since Dec 21, 2011
 */
public class LuceneMultiSearch extends LuceneSearch {

    private static final Logger logger = LogManager.getLogger();

    private final Set<Class<? extends CdmBase>> directorySelectClasses = new HashSet<>();

    /**
     * @param luceneSearch the searches to execute together as a union like search
     * @throws Exception
     */
    public LuceneMultiSearch(ILuceneIndexToolProvider toolProvider, LuceneSearch... luceneSearch) throws LuceneMultiSearchException {

        this.toolProvider = toolProvider;
        groupByField = null; //reset
        Builder queryBuilder = new Builder();

        Set<String> highlightFields = new HashSet<>();
        List<SortField> multiSearcherSortFields = new ArrayList<>();

        for(LuceneSearch search : luceneSearch){

            // Use the original type (e.g. TaxonBase), not pushAbstractBaseTypeDown(Taxon).
            // HS6 scope(TaxonBase) covers Taxon and Synonym indexes; scope(Taxon) alone drops synonyms.
            this.directorySelectClasses.add(search.getRawDirectorySelectClass());
            queryBuilder.add(search.getQuery(), Occur.SHOULD);

            // add the highlightFields from each of the sub searches
            highlightFields.addAll(Arrays.asList(search.getHighlightFields()));

            // set the class for each of the sub searches
            if(search.cdmTypeRestriction != null){
                if(cdmTypeRestriction != null && !cdmTypeRestriction.equals(search.cdmTypeRestriction)){
                    throw new LuceneMultiSearchException(
                            "LuceneMultiSearch can only handle once class restriction, but multiple given: " +
                            getCdmTypRestriction() + ", " + search.getCdmTypRestriction());
                }
                setCdmTypRestriction(search.getCdmTypRestriction());
            }

            // set the groupByField for each of the sub searches
            if(search.groupByField != null){
                if(groupByField != null && !groupByField.equals(search.groupByField)){
                    throw new LuceneMultiSearchException(
                            "LuceneMultiSearch can only handle once groupByField, but multiple given: " +
                            groupByField + ", " + search.groupByField);
                }
                groupByField = search.groupByField;
            }


            // add the sort field from each of the sub searches
            if(search.getSortFields() != null) {
                for(SortField addField : search.getSortFields()){
                    if(! multiSearcherSortFields.contains(addField)) {
                        multiSearcherSortFields.add(addField);
                    }
                }
            }
        }

        this.sortFields = multiSearcherSortFields.toArray(new SortField[multiSearcherSortFields.size()]);
        this.highlightFields = highlightFields.toArray(new String[highlightFields.size()]);
        this.query = queryBuilder.build();
    }

    @Override
    public IndexSearcher getSearcher() {

        if(searcher == null){
            List<IndexReader> readers = new ArrayList<>();
            for(Class<? extends CdmBase> type : directorySelectClasses){
                readers.add(toolProvider.getIndexReaderFor(type));
            }
            if(readers.size() > 1){
                IndexReader[] readersArray = readers.toArray(new IndexReader[readers.size()]);
                MultiReader multireader;
                try {
                    multireader = new MultiReader(readersArray, true);
                } catch (IOException e) {
                    //or do we want to force clients to handle the IOs?
                    throw new RuntimeException(e);
                }
                searcher = new IndexSearcher(multireader);
            } else {
                searcher = new IndexSearcher(readers.get(0));
            }
        }

        return searcher;
    }

    /**
     * Returns a shared analyzer suitable for all indexes in this multi-search.
     * Mixed scopes (e.g. {@code TaxonBase} + {@code DescriptionElementBase}) may
     * expose different {@code IndexingScopedAnalyzer}s in HS6; fall back to
     * {@link StandardAnalyzer} which matches {@code AnalyzerNames.DEFAULT}.
     */
    @Override
    public Analyzer getAnalyzer() {
        Analyzer analyzer = null;
        for(Class<? extends CdmBase> type : directorySelectClasses){
            Analyzer a = toolProvider.getAnalyzerFor(type);
            if(analyzer != null && !analyzer.getClass().equals(a.getClass())){
                return new StandardAnalyzer();
            }
            analyzer = a;
        }
        return analyzer;
    }
}
