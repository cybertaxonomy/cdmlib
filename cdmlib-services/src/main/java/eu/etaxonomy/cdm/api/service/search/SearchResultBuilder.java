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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.hibernate.criterion.Criterion;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

/**
 * @author Andreas Kohlbecker
 * @date Jan 6, 2012
 *
 */
public class SearchResultBuilder implements ISearchResultBuilder {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder#createResultSetFromIds(eu.etaxonomy.cdm.search.LuceneSearch, org.apache.lucene.search.TopDocs, eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao, java.lang.String)
     */
    private Query query;
    /**
     * fragmentNumber - max number of sentence fragments to return
     */
    private int fragmentNumber = 5;
    /**
     * fragmentSize - the max number of characters for each fragment
     */
    private int fragmentSize = 100;
    private LuceneSearch luceneSearch;

    /**
     * Use this constructor if you do not wish to retrieve highlighted terms found in the best sections of a text.
     * @param luceneSearch
     */
    public SearchResultBuilder(LuceneSearch luceneSearch){
        this.luceneSearch = luceneSearch;
    }

    /**
     * @param luceneSearch
     * @param query the Query will be used to highlight matching fragments if the <code>highlightFields</code> property is supplied to
     * {@link #createResultSet(TopDocs, String[], ICdmEntityDao, String, List)}
     */
    public SearchResultBuilder(LuceneSearch luceneSearch, Query query){
        this.luceneSearch = luceneSearch;
        this.query = query;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder#createResultSetFromIds(org.apache.lucene.search.TopDocs, java.lang.String[], eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao, java.lang.String, java.util.List)
     */
    public <T extends CdmBase> List<SearchResult<T>> createResultSet(TopDocs topDocsResultSet,
            String[] highlightFields, ICdmEntityDao<T> dao, String idField, List<String> propertyPaths) throws CorruptIndexException, IOException {

        List<SearchResult<T>> searchResults = new ArrayList<SearchResult<T>>();

        SearchResultHighligther highlighter = null;
        if(highlightFields  != null && highlightFields.length > 0){
            highlighter = new SearchResultHighligther();

        }


        for (ScoreDoc scoreDoc : topDocsResultSet.scoreDocs) {
            Document doc = luceneSearch.getSearcher().doc(scoreDoc.doc);
            String[] idStrings = doc.getValues(idField);
            SearchResult<T> searchResult = new SearchResult<T>(doc);
            searchResult.setScore(scoreDoc.score);

            //TODO use findByUuid(List<UUID> uuids, List<Criterion> criteria, List<String> propertyPaths)
            //      instead or even better a similar findById(List<Integer> ids) however this is not yet implemented
            if(idStrings.length > 0){
                T entity = dao.load(Integer.valueOf(idStrings[0]), propertyPaths);
                searchResult.setEntity(entity);
            }

            if(highlighter != null){
                Map<String, String[]> fieldFragmentMap = highlighter.getFragmentsWithHighlightedTerms(luceneSearch.getAnalyzer(), query, highlightFields, doc, fragmentNumber, fragmentSize);
                searchResult.setFieldHighlightMap(fieldFragmentMap);
            }

            searchResults.add(searchResult);
        }

        return searchResults;
    }

}
