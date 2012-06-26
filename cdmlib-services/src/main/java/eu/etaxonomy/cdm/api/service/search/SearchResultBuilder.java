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
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.hibernate.criterion.Criterion;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.search.LuceneSearch;

/**
 * @author Andreas Kohlbecker
 * @date Jan 6, 2012
 *
 */
@Component
public class SearchResultBuilder implements ISearchResultBuilder {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder#createResultSetFromIds(eu.etaxonomy.cdm.search.LuceneSearch, org.apache.lucene.search.TopDocs, eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao, java.lang.String)
     */
    /**
     * @param luceneSearch
     * @param topDocsResultSet
     * @param dao
     * @param idField
     * @return
     * @throws CorruptIndexException
     * @throws IOException
     */
    public <T extends CdmBase> List<SearchResult<T>> createResultSetFromIds(LuceneSearch luceneSearch, TopDocs topDocsResultSet,
            ICdmEntityDao<T> dao, String idField) throws CorruptIndexException, IOException {

        List<SearchResult<T>> searchResults = new ArrayList<SearchResult<T>>();

        for (ScoreDoc scoreDoc : topDocsResultSet.scoreDocs) {
            Document doc = luceneSearch.getSearcher().doc(scoreDoc.doc);
            String[] idStrings = doc.getValues(idField);
            SearchResult<T> searchResult = new SearchResult<T>(doc);
            //TODO use findByUuid(List<UUID> uuids, List<Criterion> criteria, List<String> propertyPaths)
            //      instead or even better a similar findById(List<Integer> ids) however this is not yet implemented
            if(idStrings.length > 0){
                T entity = dao.findById(Integer.valueOf(idStrings[0]));
                searchResult.setEntity(entity);
            }
            searchResults.add(searchResult);
        }

        return searchResults;
    }

}
