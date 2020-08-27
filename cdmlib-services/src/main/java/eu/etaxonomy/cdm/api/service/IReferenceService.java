/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


public interface IReferenceService extends IIdentifiableEntityService<Reference> {

	/**
	 * Returns a Paged List of Reference instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 *
	 * @param clazz filter the results by class (or pass null to return all Reference instances)
	 * @param queryString
	 * @param pageSize The maximum number of references returned (can be null for all matching references)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager Reference instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	@Override
    public Pager<Reference> search(Class<? extends Reference> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a map that holds uuid, titleCache pairs of all references in the current database
	 *
	 * @return
	 * 			a <code>Map</code> containing uuid and titleCache of references
	 */
	public List<UuidAndTitleCache<Reference>> getUuidAndTitle();

	/**
	 * lists all references marked as "publish" and not used as nomenclatural references
	 */
	public List<Reference> listReferencesForPublishing();

	/**
	 * Lists all references used as nomenclatural references in at least 1 {@link TaxonName}
	 */
	public List<Reference> listNomenclaturalReferences();

	/**
	 * returns
	 *
	 * <ol>
	 * <li>all taxa directly covered by this reference</li>
	 * <li>all taxa covered by the according in references of this reference</li>
	 * </ol>
	 *
	 * searches for taxa using the following relations:
	 * <ul>
	 * <li>taxon.name.nomenclaturalSource.citation</li>
	 * <li>taxon.descriptions.descriptionElement.sources.citation</li>
	 * <li>taxon.descriptions.descriptionSources</li>
	 * <li>taxon.name.descriptions.descriptionElement.sources</li>
	 * <li>taxon.name.descriptions.descriptionSources</li>
	 * </ul>
	 *
	 * @param reference
	 * @param includeSubordinateReferences TODO
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonBase> listCoveredTaxa(Reference reference, boolean includeSubordinateReferences, List<String> propertyPaths);


    public List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCache(Integer limit, String pattern);

    /**
     * @param limit
     * @param pattern
     * @param type
     * @return
     */
   public List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCache(Integer limit, String pattern, ReferenceType type);
   /**
    * @param limit
    * @param pattern
    * @param type
    * @return
    */
    public List<UuidAndTitleCache<Reference>> getUuidAndTitleCache(Integer limit, String pattern, ReferenceType type);

    /**
     * @param identifier
     * @param identifierType
     * @param matchmode
     * @param limit
     * @return
     */
    public List<IdentifiedEntityDTO<Reference>> listByIdentifierAbbrev(String identifier, DefinedTerm identifierType,
        MatchMode matchmode, Integer limit);

    /**
     * @param limit
     * @param pattern
     * @param type
     * @return
     */
    List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCacheForAuthor(Integer limit, String pattern,
            ReferenceType type);

    /**
     * @param uuids
     *
     * @return
     */
    List<UuidAndTitleCache<Reference>> getUuidAndTitleCacheForUUIDS(Set<UUID> uuids);

    /**
     * @param identifier
     * @param identifierType
     * @param matchmode
     * @param limit
     * @return
     */
    List<IdentifiedEntityDTO<Reference>> listByIdentifierAndTitleCacheAbbrev(String identifier,
            DefinedTerm identifierType, MatchMode matchmode, Integer limit);

    /**
     * @param uuids
     * @param refType
     * @return
     */
    List<UuidAndTitleCache<Reference>> getUuidAndTitleCacheForUUIDS(Set<UUID> uuids, ReferenceType refType);

    /**
     * @param config
     * @return
     */
    List<Reference> findByTitleAndAbbrevTitle(IIdentifiableEntityServiceConfigurator<Reference> config);

}
