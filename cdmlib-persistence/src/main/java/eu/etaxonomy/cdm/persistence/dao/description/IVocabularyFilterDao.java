/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.filter.VocabularyFilter;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeFilterDao;

/**
 * TODO very similar to {@link ITaxonNodeFilterDao}. Maybe we can combine it.
 *
 * @author a.mueller
 * @date 02.09.2022
 */
public interface IVocabularyFilterDao {

    /**
     * Counts the number of vocabulary IDs returned
     * when calling {@link #listUuids(VocabularyFilter)}
     * @param filter the vocabulary filter
     * @return Count of vocabularies
     */
    public long count(VocabularyFilter filter);

    /**
     * Retrieve vocabulary {@link UUID uuids} defined by a
     * {@link VocabularyFilter vocabulary filter}.
     * @param filter the vocabulary filter
     * @return List of vocabulary {@link UUID uuids}
     */
    public List<UUID> listUuids(VocabularyFilter filter);


    /**
     * Retrieve vocabulary IDs defined by a
     * {@link VocabularyFilter vocabulary filter}.
     * @param filter the vocabulary filter
     * @return List of vocabulary IDs
     */
    public List<Integer> idList(VocabularyFilter filter);
}
