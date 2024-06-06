/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.metadata.DistributionDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author k.luther
 * @since 30.11.2018
 */
public class TaxonDescriptionDTO implements Serializable{

    private static final long serialVersionUID = -4440059497898077690L;
    UUID taxonUUID;
    Set<TaxonDescription> descriptions = new HashSet<>();

    public TaxonDescriptionDTO(Taxon taxon, DistributionDescription descHandling){
       this.taxonUUID = taxon.getUuid();

        for (TaxonDescription desc: taxon.getDescriptions()){
            if (desc.isDefault()) {
                descriptions.add(desc);
            }
        }
       // if (descriptions.isEmpty() || descHandling.equals(DistributionDescription.UseAlreadyExisting)) {
            for (TaxonDescription desc: taxon.getDescriptions()){
                if (desc.isComputed()) {
                    continue;
                }
                for (DescriptionElementBase element: desc.getElements()){
                    if (element instanceof Distribution){
                        descriptions.add(desc);
                    }
                }
            }
       // }
    }

    public Set<TaxonDescription> getDescriptions() {
        return descriptions;
    }
    public void setDescriptions(Set<TaxonDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public UUID getTaxonUUID() {
        return taxonUUID;
    }
    public void setTaxonUUID(UUID taxonUUID) {
        this.taxonUUID = taxonUUID;
    }
}