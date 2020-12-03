/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate.replace;

import java.util.List;

import org.hibernate.Session;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface ReferringObjectMetadata {
	public List<CdmBase> getReferringObjects(CdmBase x,Session session);

	public void replace(CdmBase referringObject, CdmBase x, CdmBase y) throws IllegalArgumentException, IllegalAccessException;

	public Class<? extends CdmBase> getType();

	public String getFieldName();

}
