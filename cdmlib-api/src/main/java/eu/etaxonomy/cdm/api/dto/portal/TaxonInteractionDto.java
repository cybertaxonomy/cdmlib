/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @date 13.02.2023
 */
public class TaxonInteractionDto extends FactDtoBase {

    private String description;

    private List<TaggedText> taxon = new ArrayList<>();
    private UUID taxonUuid;

    // ****************** GETTER / SETTER *****************************/

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public List<TaggedText> getTaxon() {
        return taxon;
    }
    public void setTaxon(List<TaggedText> taxon) {
        this.taxon = taxon;
    }

    public UUID getTaxonUuid() {
        return taxonUuid;
    }
    public void setTaxonUuid(UUID taxonUuid) {
        this.taxonUuid = taxonUuid;
    }
}