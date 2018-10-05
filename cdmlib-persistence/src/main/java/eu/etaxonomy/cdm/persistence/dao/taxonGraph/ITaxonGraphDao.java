/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.taxonGraph;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;

/**
 * @author a.kohlbecker
 * @since Sep 26, 2018
 *
 */
public interface ITaxonGraphDao {

    /**
    *
    * @param fromTaxonUuid
    *  Can be <code>null</code> to retrieve all edges having the toName as target.
    * @param toTaxonUuid
    *  Can be <code>null</code> to retrieve all edges originating from the fromName as target.
    * @param type
    * @param includeUnpublished
    * @return
    */
   List<TaxonGraphEdgeDTO> listTaxonGraphEdgeDTOs(UUID fromTaxonUuid, UUID toTaxonUuid, TaxonRelationshipType type,
           boolean includeUnpublished, Integer pageSize, Integer pageIndex);

   /**
    * @param fromTaxonUuid
    *  Can be <code>null</code> to retrieve all edges having the toName as target.
    * @param toTaxonUuid
    *  Can be <code>null</code> to retrieve all edges originating from the fromName as target.
    * @param type
    * @param includeUnpublished
    * @return
    */
   long countTaxonGraphEdgeDTOs(UUID fromTaxonUuid, UUID toTaxonUuid, TaxonRelationshipType type,
           boolean includeUnpublished);

    List<TaxonGraphEdgeDTO> edges(UUID fromtaxonUuid, UUID toTaxonUuid, boolean includeUnpublished) throws TaxonGraphException;

    List<TaxonGraphEdgeDTO> edges(TaxonName fromName, TaxonName toName, boolean includeUnpublished) throws TaxonGraphException;

}
