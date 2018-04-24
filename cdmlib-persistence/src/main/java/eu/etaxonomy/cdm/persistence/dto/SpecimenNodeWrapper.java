/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author pplitzner
 * @since Apr 20, 2018
 *
 */
public class SpecimenNodeWrapper implements Serializable{

    private static final long serialVersionUID = -7137923139085928830L;

    private UuidAndTitleCache<SpecimenOrObservationBase> uuidAndTitleCache;
    private TaxonNode taxonNode;
    public SpecimenNodeWrapper(UuidAndTitleCache<SpecimenOrObservationBase> uuidAndTitleCache, TaxonNode taxonNode) {
        super();
        this.uuidAndTitleCache = uuidAndTitleCache;
        this.taxonNode = taxonNode;
    }
    public UuidAndTitleCache<SpecimenOrObservationBase> getUuidAndTitleCache() {
        return uuidAndTitleCache;
    }
    public TaxonNode getTaxonNode() {
        return taxonNode;
    }

}
