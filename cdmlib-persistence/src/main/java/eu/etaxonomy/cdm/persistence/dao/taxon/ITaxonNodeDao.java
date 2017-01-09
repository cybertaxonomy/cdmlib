/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.taxon;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.TreeIndex;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author a.mueller
 *
 */
public interface ITaxonNodeDao extends IAnnotatableDao<TaxonNode> {

	public UUID delete(TaxonNode persistentObject, boolean deleteChildren);

	/**
    *
    * @return
    */
   public List<TaxonNode> getTaxonOfAcceptedTaxaByClassification(Classification classification, Integer start, Integer end);

    /**
     * @param classification
     * @return
     */
    public int countTaxonOfAcceptedTaxaByClassification(Classification classification);

    /**
     * Lists all direct child nodes of the given {@link UuidAndTitleCache} which
     * represents the parent {@link TaxonNode}
     * @param parent a UuidAndTitleCache object which represents a parent {@link TaxonNode}
     * @return a list of UuidAndTitleCache objects that represent children of the
     * parent
     */
    public List<UuidAndTitleCache<TaxonNode>> listChildNodesAsUuidAndTitleCache(UuidAndTitleCache<TaxonNode> parent);

    /**
     * Retrieves the parent node of the {@link TaxonNode} represented by the given {@link UuidAndTitleCache}.
     * @param child the child for which the parent should be retrieved
     * @return an UuidAndTitleCache object representing the parent node
     */
    public UuidAndTitleCache<TaxonNode> getParentUuidAndTitleCache(UuidAndTitleCache<TaxonNode> child);

    /**
     * Retrieves a list of {@link UuidAndTitleCache} objects that have a matching titleCache
     * @param limit the maximum results
     * @param pattern the titleCache that is searched for
     * @param classificationUuid if specified only nodes of this classification are retrieved
     * @return a list of matches
     */
    public List<UuidAndTitleCache<TaxonNode>> getUuidAndTitleCache(Integer limit, String pattern, UUID classificationUuid);

    public List<TaxonNode> listChildrenOf(TaxonNode node, Integer pageSize, Integer pageIndex, List<String> propertyPaths, boolean recursive);

    public abstract Long countChildrenOf(TaxonNode node, Classification classification, boolean recursive);

    /**
     * Returns the of TaxonNodeAgentRelation entities which are associated with the TaxonNode for the
     * given TaxonUuid in the specified Classification.
     *
     * @param taxonUuid
     * @param agentUuid TODO
     * @param relTypeUuid TODO
     * @param start
     * @param limit
     * @param propertyPaths
     * @param rankId TODO
     * @param classification
     * @return
     */
    List<TaxonNodeAgentRelation> listTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid,
            UUID agentUuid, UUID rankUuid, UUID relTypeUuid, Integer start, Integer limit, List<String> propertyPaths);

    /**
     * Returns the number of TaxonNodeAgentRelation entities which are associated with the TaxonNode for the
     * given TaxonUuid in the specified Classification.
     *
     * @param taxonUuid
     * @param agentUuid TODO
     * @param relTypeUuid TODO
     * @param rankId TODO
     * @param classification
     * @return
     */
    long countTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid, UUID rankUuid, UUID relTypeUuid);

    /**
     * Computes a map treeIndex->rank(sortIndex) for each given taxon node treeIndex. Required by #5957.
     * If the taxon represented by the treeindex is not in the given rank range no record is returned for the given
     * treeindex.
     *
     * @param treeIndex the list of treeIndexes
     * @param minRankOrderIndex min rank
     * @param maxRankOrderIndex max rank
     * @return
     */
    Map<TreeIndex, Integer> rankOrderIndexForTreeIndex(List<TreeIndex> treeIndex, Integer minRankOrderIndex,
            Integer maxRankOrderIndex);

    /**
     * For a given set of taxon node tree indexes the uuid and title cache of the taxon represented
     * by this treeindex is returned.
     * @param treeIndexSet set of taxon node tree indexes
     * @return map with treeindex and uuidAndTitleCache of the represented taxon
     */
    Map<TreeIndex, UuidAndTitleCache<?>> taxonUuidsForTreeIndexes(Collection<TreeIndex> treeIndexSet);

    /**
     * @param ref
     * @return
     */
    public Set<Taxon> setSecundumForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, Reference newSec, boolean overwriteExisting, boolean includeSharedTaxa, boolean emptyDetail);

    /**
     * @param ref
     */
    public  Set<Synonym> setSecundumForSubtreeSynonyms(TreeIndex subTreeIndex, Reference newSec, boolean overwriteExisting, boolean includeSharedTaxa, boolean emptyDetail);

}
