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
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;

/**
 * This SearchResultHighligther is using the
 * QueryScorer by default even if the SpanScorer is meant to be the new default scorer in Lucene,
 * see https://issues.apache.org/jira/browse/LUCENE-1685 and https://issues.apache.org/jira/browse/LUCENE-2013.
 * The SpanScorer was causing problems with phrase queries (see https://dev.e-taxonomy.eu/trac/ticket/2961)
 * whereas the QueryScorer was returning good results.
 * <p>
 * This SearchResultHighligther can be switched to use the SpanScorer: {@link #setUseSpanScorer(boolean)}
 * <p>
 * Based on work of Nicholas Hrycan
 * see http://code.google.com/p/hrycan-blog/source/browse/trunk/lucene-highlight/src/com/hrycan/search/HighlighterUtil.java
 *
 *
 * @author Andreas Kohlbecker
 *
 */
public class SearchResultHighligther {

    public static final Logger logger = Logger.getLogger(SearchResultHighligther.class);

    private boolean useSpanScorer = true;

    public boolean isUseSpanScorer() {
        return useSpanScorer;
    }

    public void setUseSpanScorer(boolean useSpanScorer) {
        this.useSpanScorer = useSpanScorer;
    }

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
            String[] fragments = getFragmentsWithHighlightedTerms(stream, query, fieldName, fieldContents, fragmentNumber, fragmentSize);

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
            String[] fragments = getFragmentsWithHighlightedTerms(stream, query, fieldName, fieldContents, fragmentNumber, fragmentSize);

            return fragments;
    }

    /**
     * @param stream
     * @param query - query object created from user's input
     * @param fieldName - name of the field containing the text to be fragmented
     * @param fieldContents - contents of fieldName
     * @param fragmentNumber - max number of sentence fragments to return
     * @param fragmentSize - the max number of characters for each fragment
     * @return
     * @throws IOException
     */
    private String[] getFragmentsWithHighlightedTerms(TokenStream stream, Query query, String fieldName, String fieldContents, int fragmentNumber,
            int fragmentSize) throws IOException {

        /*
         * for (int i = 0; i < hits.length(); i++) {
       String text = hits.doc(i).get(FIELD_NAME);
       CachingTokenFilter tokenStream = new CachingTokenFilter(analyzer.tokenStream(FIELD_NAME,
           new StringReader(text)));
       Highlighter highlighter = new Highlighter(this, new SpanScorer(query, FIELD_NAME, tokenStream));
       highlighter.setTextFragmenter(new SimpleFragmenter(40));
       tokenStream.reset();

       String result = highlighter.getBestFragments(tokenStream, text, maxNumFragmentsRequired,
           "...");
         */

        Fragmenter fragmenter = null;
        Highlighter highlighter = null;
        if(false && useSpanScorer){
//            new CachingTokenFilter(stream);
//            SpanScorer scorer = new SpanScorer(query, fieldName);
//            highlighter = null;// new Highlighter(this, scorer);
//            fragmenter = null; //new SimpleSpanFragmenter((SpanScorer)scorer, fragmentSize);
        } else {
            Scorer scorer;
            scorer = new QueryScorer(query, fieldName);
            fragmenter = new SimpleFragmenter(fragmentSize);
            highlighter = new Highlighter(scorer);
        }

        highlighter.setTextFragmenter(fragmenter);
        highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);

        String[] fragments = null;
        try {
            fragments = highlighter.getBestFragments(stream, fieldContents, fragmentNumber);
        } catch (InvalidTokenOffsetsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fragments;
    }

}

