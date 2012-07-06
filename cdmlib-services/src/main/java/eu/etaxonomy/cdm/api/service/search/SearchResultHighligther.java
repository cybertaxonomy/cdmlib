// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.SpanScorer;
import org.apache.lucene.search.highlight.TokenSources;

/**
 * Based on work of Nicholas Hrycan
 * see http://code.google.com/p/hrycan-blog/source/browse/trunk/lucene-highlight/src/com/hrycan/search/HighlighterUtil.java
 *
 * @author Andreas Kohlbecker
 *
 */
public class SearchResultHighligther {

    public static final Logger logger = Logger.getLogger(SearchResultHighligther.class);

    private Searcher searcher;

    public Map<String,String[]> getFragmentsWithHighlightedTerms(Analyzer analyzer, Query query, String[] fieldNames,  Document doc,  int fragmentNumber, int fragmentSize){

        Map<String,String[]> fieldHighlightMap = new HashMap<String, String[]>();
        String[] values;
        String fieldContents;
        String[] fragments;

        try {
            for(String fieldName : fieldNames){
                values = doc.getValues(fieldName);
                if(values.length == 0){
                    continue;
                }
                fieldContents = StringUtils.join(values, ' ');
                fragments = getFragmentsWithHighlightedTerms(analyzer, query, fieldName, fieldContents, fragmentNumber, fragmentSize);
                fieldHighlightMap.put(fieldName, fragments);
            }
        } catch (CorruptIndexException e) {
            logger.error("Error on retrieving highlighted fragments", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Error on retrieving highlighted fragments", e);
        }

        return fieldHighlightMap;
    }

    /**
     * Generates contextual fragments.  Assumes term vectors not stored in the index.
     * @param analyzer - analyzer used for both indexing and searching
     * @param query - query object created from user's input
     * @param fieldName - name of the field in the lucene doc containing the text to be fragmented
     * @param fieldContents - contents of fieldName
     * @param fragmentNumber - max number of sentence fragments to return
     * @param fragmentSize - the max number of characters for each fragment
     * @return
     * @throws IOException
     */
    public String[] getFragmentsWithHighlightedTerms(Analyzer analyzer, Query query,
                    String fieldName, String fieldContents, int fragmentNumber, int fragmentSize) throws IOException {

            TokenStream stream = TokenSources.getTokenStream(fieldName, fieldContents, analyzer);
            SpanScorer scorer = new SpanScorer(query, fieldName, new CachingTokenFilter(stream));
            Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmentSize);

            Highlighter highlighter = new Highlighter(scorer);
            highlighter.setTextFragmenter(fragmenter);
            highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);

            String[] fragments = highlighter.getBestFragments(stream, fieldContents, fragmentNumber);

            return fragments;
    }


    /**
     * Generates contextual fragments.
     * @param termPosVector - Term Position Vector for fieldName
     * @param query - query object created from user's input
     * @param fieldName - name of the field containing the text to be fragmented
     * @param fieldContents - contents of fieldName
     * @param fragmentNumber - max number of sentence fragments to return
     * @param fragmentSize - the max number of characters for each fragment
     * @return
     * @return
     * @throws IOException
     */
    public String[] getFragmentsWithHighlightedTerms(TermPositionVector termPosVector, Query query,
                    String fieldName, String fieldContents, int fragmentNumber, int fragmentSize) throws IOException  {

            TokenStream stream = TokenSources.getTokenStream(termPosVector);
            SpanScorer scorer = new SpanScorer(query, fieldName, new CachingTokenFilter(stream));
            Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmentSize);
            Highlighter highlighter = new Highlighter(scorer);
            highlighter.setTextFragmenter(fragmenter);
            highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);

            String[] fragments = highlighter.getBestFragments(stream, fieldContents, fragmentNumber);

            return fragments;
    }

}

