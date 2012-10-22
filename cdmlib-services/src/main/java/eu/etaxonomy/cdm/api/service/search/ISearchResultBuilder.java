package eu.etaxonomy.cdm.api.service.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.grouping.TopGroups;

import eu.etaxonomy.cdm.api.service.search.LuceneSearch.TopGroupsWithMaxScore;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

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
     * The firts Cdm enitity id found in the specified <code>idFields</code> of the Lucene documents will be used to load
     * the referenced Cdm entities into the <code>SearchResult</code>s.
     *
     * @param topGroupsResultSet
     * @param highlightFields
     * @param dao
     * @param idFields a map of class names as key and entity id fields as values
     * @param propertyPaths
     * @return
     * @throws CorruptIndexException
     * @throws IOException
     */
    public abstract <T extends CdmBase> List<SearchResult<T>> createResultSet(TopGroupsWithMaxScore topGroupsResultSet,
            String[] highlightFields, ICdmEntityDao<T> dao, Map<CdmBaseType, String> idFields, List<String> propertyPaths) throws CorruptIndexException, IOException;

}