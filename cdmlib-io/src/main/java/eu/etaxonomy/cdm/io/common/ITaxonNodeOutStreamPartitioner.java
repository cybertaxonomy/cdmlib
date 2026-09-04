/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 26.08.2019
 */
public interface ITaxonNodeOutStreamPartitioner {

    /**
     * Retrieve next partition. If a sortMode is given, the partition will be sorted accordingly.
     */
    public TaxonNode next(TaxonNodeFilter.TaxonNodeFilterSortMode sortMode);

    void setReadOnly(boolean readOnly);

    public void close();

}