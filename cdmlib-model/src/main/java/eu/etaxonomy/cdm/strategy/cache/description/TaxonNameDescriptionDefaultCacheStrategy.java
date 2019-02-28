/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.description;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;

/**
 * @author k.luther
 * @since 27 Feb 2019
 *
 */
public class TaxonNameDescriptionDefaultCacheStrategy extends DescriptionBaseDefaultCacheStrategy<TaxonNameDescription> {

    private static final long serialVersionUID = -5395563265358892266L;
    final static UUID uuid = UUID.fromString("8fa98b76-f52a-4f5d-aaaf-77addda5f9df");

    @Override
    protected UUID getUuid() {
        return uuid;
    }

    @Override
    public String getTitleCache(TaxonNameDescription nameDescription) {
        String title = super.getTitleCache(nameDescription);

        return title;
    }

    @Override
    protected String getDescriptionName() {
        return "Factual data set";
    }

    @Override
    protected IdentifiableEntity getDescriptionEntity(TaxonNameDescription description) {
        return description.getTaxonName();
    }

}
