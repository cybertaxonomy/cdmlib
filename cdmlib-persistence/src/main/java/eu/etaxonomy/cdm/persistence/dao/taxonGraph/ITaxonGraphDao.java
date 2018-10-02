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
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;

/**
 * @author a.kohlbecker
 * @since Sep 26, 2018
 *
 */
public interface ITaxonGraphDao {


    /**
     * ONLY for tests!!!
     * @param uuid
     */
    @Deprecated
    void setSecReferenceUUID(UUID uuid);

    List<TaxonGraphEdgeDTO> edges(UUID fromtaxonUuid, UUID toTaxonUuid, boolean includeUnpublished) throws TaxonGraphException;

    List<TaxonGraphEdgeDTO> edges(TaxonName fromName, TaxonName toName, boolean includeUnpublished) throws TaxonGraphException;


}
