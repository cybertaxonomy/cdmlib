// $Id$
/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.Set;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author pplitzner
 * @since 16.04.2018
 *
 */
public class TaxonRowWrapperDTO extends RowWrapperDTO<TaxonDescription> {

    private static final long serialVersionUID = 5198447592554976471L;

    Set<DescriptionBaseDto> taxonDescriptions;

    public TaxonRowWrapperDTO(DescriptionBaseDto description, TaxonNodeDto taxonNode, Set<DescriptionBaseDto> taxonDescriptions) {
        super(description, taxonNode);
        this.taxonDescriptions = taxonDescriptions;

    }

    public Set<DescriptionBaseDto> getTaxonDescriptions() {
        return taxonDescriptions;
    }


    public void setTaxonDescriptions(Set<DescriptionBaseDto> taxonDescriptions) {
        this.taxonDescriptions = taxonDescriptions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaxonRowWrapperDTO){
            return ((TaxonRowWrapperDTO)obj).getDescription().getDescriptionUuid().equals(this.getDescription().getDescriptionUuid());
        } else {
            return false;
        }
    }

}
