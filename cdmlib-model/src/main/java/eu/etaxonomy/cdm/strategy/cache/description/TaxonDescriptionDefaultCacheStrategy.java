/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.description;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.TaxonDescription;

public class TaxonDescriptionDefaultCacheStrategy extends DescriptionBaseDefaultCacheStrategy<TaxonDescription>{

    private static final long serialVersionUID = -3631745932476721570L;

    final static UUID uuid = UUID.fromString("0517ae48-597d-4d6b-9f18-8752d689720d");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    public String getTitleCache(TaxonDescription taxonDescription) {
		String title = super.getTitleCache(taxonDescription);
		int secPos = title.indexOf("sec.");
		if (secPos > 2){
		    title = title.substring(0, secPos).trim();
		}
		return title;
	}

	@Override
	protected String getDescriptionName() {
	    return "Factual data set";
	}

	@Override
	protected IdentifiableEntity getDescriptionEntity(TaxonDescription description) {
	    return description.getTaxon();
	}

}
