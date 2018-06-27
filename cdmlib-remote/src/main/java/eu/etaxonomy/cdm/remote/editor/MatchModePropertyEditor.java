/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import eu.etaxonomy.cdm.persistence.query.MatchMode;

public class MatchModePropertyEditor extends PropertyEditorSupport {

	@Override
    public void setAsText(String name) {
		setValue(MatchMode.valueOf(name == null ? null : name.toUpperCase()));
	}

}
