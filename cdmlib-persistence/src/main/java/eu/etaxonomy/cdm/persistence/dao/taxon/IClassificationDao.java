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

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

/**
 * @author a.mueller
 *
 */
public interface IClassificationDao extends IIdentifiableDao<Classification> {

    /**
     * Loads all TaxonNodes of the specified tree for a given Rank.
     * If a branch does not contain a TaxonNode with a TaxonName at the given
     * Rank the node associated with the next lower Rank is taken as root node.
     * If the <code>rank</code> is null the absolute root nodes will be returned.
     *
     * @param classification
     * @param rank may be null
     * @param limit The maximum number of objects returned (can be null for all matching objects)
     * @param start The offset from the start of the result set (0 - based, can be null -
     * 		equivalent of starting at the beginning of the recordset)
     * @param propertyPaths
     * @return
     */
    public List<TaxonNode> loadRankSpecificRootNodes(Classification classification, Rank rank, Integer limit, Integer start, List<String> propertyPaths);

    public long countRankSpecificRootNodes(Classification classification, Rank rank);


}
