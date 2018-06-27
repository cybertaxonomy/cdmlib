/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.reference;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 *
 */
public interface IReferenceDao extends IIdentifiableDao<Reference>, ITitledDao<Reference> {

	public List<UuidAndTitleCache<Reference>> getUuidAndTitle();

	/**
	 * TODO candidate for harmonization: rename to listAllReferencesForPublishing
	 * @return all references marked with publish-flag
	 */
	public List<Reference> getAllReferencesForPublishing();

	/**
	 * TODO candidate for harmonization: rename to listAllNotNomenclaturalReferencesForPublishing
	 * @return all references not used as nomenclatural reference with publish flag
	 */
	public List<Reference> getAllNotNomenclaturalReferencesForPublishing();

	/**
	 * TODO candidate for harmonization: rename to listNomenclaturalReferences
	 * @return
	 */
	public List<Reference> getAllNomenclaturalReferences();

	/**
	 * recursively finds all references where the <code>referenceBase</code> given as parameter
	 * is the {@link Reference.getInReference inReference}.
	 * @param reference
	 * @return
	 */
	public List<Reference> getSubordinateReferences(Reference reference);

	/**
	 * searches for taxa using the following relations:
	 * <ul>
	 * <li>taxon.name.nomenclaturalreference</li>
	 * <li>taxon.descriptions.descriptionElement.sources.citation</li>
	 * <li>taxon.descriptions.descriptionSources</li>
	 * <li>taxon.name.descriptions.descriptionElement.sources</li>
	 * <li>taxon.name.descriptions.descriptionSources</li>
	 * </ul>
	 *
	 * @param reference
	 * @param orderHints TODO
	 * @param propertyPaths TODO
	 * @return
	 */
	public List<TaxonBase> listCoveredTaxa(Reference reference, boolean includeSubordinateReferences, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
     * @param limit
     * @param pattern
     * @param refType
     * @return
     */
    List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCache(Integer limit, String pattern, ReferenceType refType);

    /**
     * @param limit
     * @param pattern
     * @param refType
     * @return
     */
    List<UuidAndTitleCache<Reference>> getUuidAndTitleCache(Integer limit, String pattern, ReferenceType refType);

    /**
     * @param identifier
     * @param identifierType
     * @param matchmode
     * @param limit
     * @return
     */
    List<Object[]> findByIdentifierAbbrev(String identifier, DefinedTermBase identifierType, MatchMode matchmode,
            Integer limit);

    /**
     * @param limit
     * @param pattern
     * @param refType
     * @return
     */
    List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCacheForAuthor(Integer limit, String pattern,
            ReferenceType refType);

    /**
     * @param uuids
     * @return
     */
    List<UuidAndTitleCache<Reference>> getUuidAndTitle(Set<UUID> uuids);


}
