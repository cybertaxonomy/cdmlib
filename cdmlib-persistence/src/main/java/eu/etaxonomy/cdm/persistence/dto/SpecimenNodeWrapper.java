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
import java.util.UUID;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * @author pplitzner
 * @since Apr 20, 2018
 *
 */
public class SpecimenNodeWrapper implements Serializable{

    private static final long serialVersionUID = -7137923139085928830L;

    private UuidAndTitleCache<SpecimenOrObservationBase> specimenUuidAndTitleCache;
    private SpecimenOrObservationType type;
    private TaxonNodeDto taxonNode;
    private UUID taxonDescriptionUuid;

    public SpecimenNodeWrapper(UuidAndTitleCache<SpecimenOrObservationBase> uuidAndTitleCache,
            SpecimenOrObservationType type,
            TaxonNodeDto taxonNode) {
        super();
        this.specimenUuidAndTitleCache = uuidAndTitleCache;
        this.type = type;
        this.taxonNode = taxonNode;
    }
    public UuidAndTitleCache<SpecimenOrObservationBase> getUuidAndTitleCache() {
        return specimenUuidAndTitleCache;
    }
    public SpecimenOrObservationType getType() {
        return type;
    }
    public TaxonNodeDto getTaxonNode() {
        return taxonNode;
    }
    public UUID getTaxonDescriptionUuid() {
        return taxonDescriptionUuid;
    }
    public void setTaxonDescriptionUuid(UUID taxonDescriptionUuid) {
        this.taxonDescriptionUuid = taxonDescriptionUuid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
//        result = prime * result + ((taxonNode == null) ? 0 : taxonNode.hashCode());
        result = prime * result + ((specimenUuidAndTitleCache == null) ? 0 : specimenUuidAndTitleCache.getUuid().hashCode());
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
//        if (taxonNode == null) {
//            if (other.taxonNode != null) {
//                return false;
//            }
//        } else if (!taxonNode.equals(other.taxonNode)) {
//            return false;
//        }
        if (specimenUuidAndTitleCache == null) {
            if (other.specimenUuidAndTitleCache != null) {
                return false;
            }
        } else if (!specimenUuidAndTitleCache.getUuid().equals(other.specimenUuidAndTitleCache.getUuid())) {
            return false;
        }
        return true;
    }



}
