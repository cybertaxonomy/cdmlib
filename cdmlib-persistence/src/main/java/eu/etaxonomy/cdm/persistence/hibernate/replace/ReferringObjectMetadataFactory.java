/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate.replace;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface ReferringObjectMetadataFactory {

	 Set<ReferringObjectMetadata> get(Class<? extends CdmBase> type);
}
