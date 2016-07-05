/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.taxon;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dto.ClassificationLookupDTO;

/**
 * @author a.mueller
 *
 */
public interface IClassificationDao extends IIdentifiableDao<Classification> {

    /**
     * <p>
     * Lists all TaxonNodes of the specified tree for a given Rank. If a branch
     * does not contain a TaxonNode with a TaxonName at the given Rank the node
     * associated with the next lower Rank is taken as root node. If the
     * <code>rank</code> is null the absolute root nodes will be returned.
     * <p>
     * See <a href="http://dev.e-taxonomy.eu/trac/wiki/CdmClassificationRankSpecificRootnodes">http://dev.e-taxonomy.eu/trac/wiki/CdmClassificationRankSpecificRootnodes</a>
     * <p>
     * Since this method is using two queries which need to be run sequentially
     * the handling of limit and start is more complex and requires the total
     * count of the items matched by the first query is known. Therefore the
     * handling of limit and start must be managed in the service method that is
     * using this dao method.
     *
     * @param classification
     * @param rank
     *            may be null
     * @param limit
     *            The maximum number of objects returned (can be null for all
     *            matching objects)
     * @param start
     *            The offset from the start of the result set (0 - based, can be
     *            null - equivalent of starting at the beginning of the
     *            recordset)
     * @param propertyPaths
     * @param queryIndex
     *            0: execute first query, 1: excute second query, the second
     *            query is only available when parameter
     *            <code>rank != null</code>.
     * @return
     */
    public List<TaxonNode> listRankSpecificRootNodes(Classification classification, Rank rank, Integer limit, Integer start,
            List<String> propertyPaths, int queryIndex);

    public long[] countRankSpecificRootNodes(Classification classification, Rank rank);

    public List<TaxonNode> listChildrenOf(Taxon taxon, Classification classification, Integer pageSize, Integer pageIndex, List<String> propertyPaths);


    public Long countChildrenOf(Taxon taxon, Classification classification);

    public ClassificationLookupDTO classificationLookup(Classification classification);

    /**
     * @param taxon
     * @param classification
     * @param pageSize
     * @param pageIndex
     * @param propertyPaths
     * @return
     */
    List<TaxonNode> listSiblingsOf(Taxon taxon, Classification classification, Integer pageSize, Integer pageIndex,
            List<String> propertyPaths);

    /**
     * @param taxon
     * @param classification
     * @return
     */
    Long countSiblingsOf(Taxon taxon, Classification classification);

    /**
     * Returns the tree indexes for a given set of taxon uuids as a map.
     * @param classificationUuid
     * @param originalTaxonUuids
     * @return
     */
    Map<UUID, String> treeIndexForTaxonUuids( UUID classificationUuid, List<UUID> originalTaxonUuids);


}
