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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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
    public <T extends CdmBase> List<SearchResult<T>> createResultSetFromIds(LuceneSearch luceneSearch, TopDocs topDocsResultSet,
            ICdmEntityDao<T> dao, String idField) throws CorruptIndexException, IOException {

        List<SearchResult<T>> searchResults = new ArrayList<SearchResult<T>>();

        for (ScoreDoc scoreDoc : topDocsResultSet.scoreDocs) {
            Document doc = luceneSearch.getSearcher().doc(scoreDoc.doc);
            String[] idStrings = doc.getValues(idField);
            SearchResult<T> searchResult = new SearchResult<T>(doc);
            if(idStrings.length > 0){
                T entity = dao.findById(Integer.valueOf(idStrings[0]));
                searchResult.setEntity(entity);
            }
            searchResults.add(searchResult);
        }

        return searchResults;

    }


}
