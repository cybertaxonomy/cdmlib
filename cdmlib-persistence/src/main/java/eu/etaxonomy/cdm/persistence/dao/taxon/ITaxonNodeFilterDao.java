/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.taxon;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.filter.TaxonNodeFilter;

/**
 * @author a.mueller
 * @date 30.06.2017
 *
 */
public interface ITaxonNodeFilterDao {

    /**
     * Counts the number of taxon node IDs returned
     * when calling {@link #listUuids(TaxonNodeFilter)}
     * @param filter the taxon node filter
     * @return Count of taxon nodes
     */
    public long count(TaxonNodeFilter filter);

    /**
     * Retrieve taxon node {@link UUID uuids} defined by a
     * {@link TaxonNodeFilter taxon node filter}.
     * @param filter the taxon node filter
     * @return List of taxon node {@link UUID uuids}
     */
    public List<UUID> listUuids(TaxonNodeFilter filter);

}