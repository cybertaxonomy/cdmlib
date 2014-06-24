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
import java.util.UUID;

import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;

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

    public List<TaxonNode> listChildrenOf(TaxonNode node, Integer pageSize, Integer pageIndex, List<String> propertyPaths, boolean recursive);

    public abstract Long countChildrenOf(TaxonNode node, Classification classification, boolean recursive);
}
