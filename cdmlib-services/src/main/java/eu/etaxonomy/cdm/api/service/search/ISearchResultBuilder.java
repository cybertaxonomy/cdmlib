package eu.etaxonomy.cdm.api.service.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.TopDocs;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.search.LuceneSearch;

/**
 * Interface for Builder classes which create {@link SearchResult} instances, from a list of LuceneSearch {@link TopDocs}.
 *
 * @author Andreas Kohlbecker
 * @date Jan 6, 2012
 *
 */
public interface ISearchResultBuilder {

    /**
     * Creates a <code>List</code> of <code>SearchResult</code> entities from the supplied <code>TopDocs</code>.
     * The Cdm enitity id found in the specified <code>idField</code> of the Lucene documents will be used to load
     * the referenced Cdm entities into the <code>SearchResult</code>s.
     *
     * @param luceneSearch
     * @param topDocsResultSet
     * @param dao
     * @param idField
     * @return
     * @throws CorruptIndexException
     * @throws IOException
     */
    public abstract <T extends CdmBase> List<SearchResult<T>> createResultSetFromIds(LuceneSearch luceneSearch, TopDocs topDocsResultSet,
            ICdmEntityDao<T> dao, String idField) throws CorruptIndexException, IOException;

}