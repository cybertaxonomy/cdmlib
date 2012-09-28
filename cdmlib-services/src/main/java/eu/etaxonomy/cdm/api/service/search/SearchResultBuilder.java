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
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.hibernate.search.engine.DocumentBuilder;

import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

/**
 * @author Andreas Kohlbecker
 * @date Jan 6, 2012
 *
 */
public class SearchResultBuilder implements ISearchResultBuilder {

    public static final Logger logger = Logger.getLogger(SearchResultBuilder.class);

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

    /**
     * {@inheritDoc}
     *
     * <h3>NOTE:</h3> All {@link MultiTermQuery} like {@link WildcardQuery} are
     * constant score by default since Lucene 2.9, you can change that back to
     * scoring mode: <code>WildcardQuery.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE)</code>
     * This slows down the query immense or throws TooManyClauses exceptions if
     * too many terms match the wildcard.
     */
    public <T extends CdmBase> List<SearchResult<T>> createResultSet(TopDocs topDocsResultSet,
                String[] highlightFields, ICdmEntityDao<T> dao, Map<CdmBaseType, String> idFields, List<String> propertyPaths) throws CorruptIndexException, IOException {

        List<SearchResult<T>> searchResults = new ArrayList<SearchResult<T>>();

        SearchResultHighligther highlighter = null;
        if(highlightFields  != null && highlightFields.length > 0){
            highlighter = new SearchResultHighligther();
        }

        for (ScoreDoc scoreDoc : topDocsResultSet.scoreDocs) {

            Document doc = luceneSearch.getSearcher().doc(scoreDoc.doc);
            SearchResult<T> searchResult = new SearchResult<T>(doc);

            // set score values
            if(isNumber(scoreDoc.score)){
                searchResult.setScore(scoreDoc.score);
            }
            if(isNumber(topDocsResultSet.getMaxScore())){
                searchResult.setMaxScore(topDocsResultSet.getMaxScore());
            }

            //TODO use findByUuid(List<UUID> uuids, List<Criterion> criteria, List<String> propertyPaths)
            //      instead or even better a similar findById(List<Integer> ids) however this is not yet implemented
            String id = findId(idFields, doc);
            if(id != null){
                T entity = dao.load(Integer.valueOf(id), propertyPaths);
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

    /**
     * find the entity id
     *
     * @param idFields
     * @param doc
     * @return
     */
    private String findId(Map<CdmBaseType,String> idFieldMap, Document doc) {

        String docClassName = doc.getValues(DocumentBuilder.CLASS_FIELDNAME)[0];

        String id = null;
        for(CdmBaseType baseType  : idFieldMap.keySet()){
            if(baseType.getSubClassNames().contains(docClassName)){
                String[] idStrings = doc.getValues(idFieldMap.get(baseType));
                if(idStrings.length > 0 && StringUtils.isNotBlank(idStrings[0])){
                    id = idStrings[0];
                    break;
                }
            }
        }
        if(id == null){
            throw new RuntimeException("No id field name given for " + docClassName);
        }
        return id;
    }

    /**
     * @param number
     * @return
     */
    private boolean isNumber(Float number) {
        return !Double.isNaN(number) && !Double.isInfinite(number);
    }

}
