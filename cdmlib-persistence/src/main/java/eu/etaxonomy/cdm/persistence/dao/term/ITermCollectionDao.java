/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.term;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.TermSearchField;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermGraphBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dto.TermCollectionDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author a.mueller
 * @date 12.04.2023
 */
public interface ITermCollectionDao extends IIdentifiableDao<TermCollection> {

    /**
     * Returns a set of terms available in the given term graph by a label pattern
     */
    public <TERM extends DefinedTermBase> List<TERM> listTerms(Class<TERM> type, List<TermGraphBase> graphs,
            Integer limit, String pattern, TermSearchField labelType, Language lang);

    public <S extends TermCollection> List<UuidAndTitleCache<S>> getUuidAndTitleCacheByTermType(Class<S> clazz, TermType termType, Integer limit,
            String pattern);

    /**
     * @param termTypes
     * @param pattern
     * @param includeSubtypes
     * @return
     */
    List<TermCollectionDto> findCollectionDtoByTermTypes(Set<TermType> termTypes, String pattern,
            boolean includeSubtypes);

    /**
     * @param termTypes
     * @param includeSubtypes
     * @return
     */
    public List<TermCollectionDto> findCollectionDtoByTermTypes(Set<TermType> termTypes, boolean includeSubtypes);

    /**
     * @param termTypes
     * @param includeSubtypes
     * @return
     */
    public List<TermCollectionDto> findCollectionDtoByUuids(List<UUID> uuids);
}
