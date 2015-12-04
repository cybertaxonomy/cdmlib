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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.TokenStreamFromTermVector;

/**
 *
 * @author Andreas Kohlbecker
 *
 */
public class SearchResultHighligther {

    public static final Logger logger = Logger.getLogger(SearchResultHighligther.class);

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

            TokenStream stream = analyzer.tokenStream(fieldName, fieldContents);
            String[] fragments = getFragmentsWithHighlightedTerms(stream, query, fieldName, fieldContents, fragmentNumber, fragmentSize);

            return fragments;
    }


    /**
     * Generates contextual fragments.
     * @param terms - Terms obtained from the index reader by e.g.: <code>Terms terms = ir.getTermVector(docID, "text");</code>
     * @param query - query object created from user's input
     * @param fieldName - name of the field containing the text to be fragmented
     * @param fieldContents - contents of fieldName
     * @param fragmentNumber - max number of sentence fragments to return
     * @param fragmentSize - the max number of characters for each fragment
     * @return
     * @return
     * @throws IOException
     */

    public String[] getFragmentsWithHighlightedTerms(Terms terms, Query query,
                    String fieldName, String fieldContents, int fragmentNumber, int fragmentSize) throws IOException  {

            // ---- snipped
           // from within deprecated method org.apache.lucene.search.highlight.TokenSources.getTokenStream(Terms tpv)
            if (!terms.hasOffsets()) {
                throw new IllegalArgumentException("Highlighting requires offsets from the TokenStream.");
                //TokenStreamFromTermVector can handle a lack of offsets if there are positions. But
                // highlighters require offsets, so we insist here.
            }

            TokenStream stream = new TokenStreamFromTermVector(terms, -1);
            // --- snap END

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


        Scorer scorer = new QueryScorer(query, fieldName);
        Fragmenter fragmenter = new SimpleFragmenter(fragmentSize);
        Highlighter highlighter = new Highlighter(scorer);

        highlighter.setTextFragmenter(fragmenter);
        highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);

        String[] fragments = null;
        try {
            fragments = highlighter.getBestFragments(stream, fieldContents, fragmentNumber);
        } catch (InvalidTokenOffsetsException e) {
            //should never happen
            logger.error("InvalidTokenOffsetsException", e);
        }
        return fragments;
    }

}

