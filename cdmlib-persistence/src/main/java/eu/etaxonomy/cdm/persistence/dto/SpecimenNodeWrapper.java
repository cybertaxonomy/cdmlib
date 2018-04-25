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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((taxonNode == null) ? 0 : taxonNode.hashCode());
        result = prime * result + ((uuidAndTitleCache == null) ? 0 : uuidAndTitleCache.getUuid().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SpecimenNodeWrapper other = (SpecimenNodeWrapper) obj;
        if (taxonNode == null) {
            if (other.taxonNode != null) {
                return false;
            }
        } else if (!taxonNode.equals(other.taxonNode)) {
            return false;
        }
        if (uuidAndTitleCache == null) {
            if (other.uuidAndTitleCache != null) {
                return false;
            }
        } else if (!uuidAndTitleCache.getUuid().equals(other.uuidAndTitleCache.getUuid())) {
            return false;
        }
        return true;
    }



}
