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

import eu.etaxonomy.cdm.model.description.SpecimenDescription;

public class SpecimenDescriptionDefaultCacheStrategy extends DescriptionBaseDefaultCacheStrategy<SpecimenDescription> {
    private static final long serialVersionUID = 310092633142719872L;

    final static UUID uuid = UUID.fromString("73c03fc4-0429-4ca1-b2cb-b9a56aad4d22");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
	protected String getDescriptionName() {
	    return "Specimen description";
	}

}
