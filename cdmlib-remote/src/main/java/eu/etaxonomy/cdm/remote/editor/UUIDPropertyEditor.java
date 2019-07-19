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
import java.util.UUID;

public class UUIDPropertyEditor extends PropertyEditorSupport  {

    private String nullRepresentation;

    public UUIDPropertyEditor() {
        super();
    }

    public UUIDPropertyEditor(String nullRepresentation){
        super();
        this.nullRepresentation = nullRepresentation;
    }

	@Override
    public void setAsText(String text) {
	    if(nullRepresentation != null && nullRepresentation.equals(text)){
	        setValue(null);
	    } else {
			setValue(UUID.fromString(text));
	    }
	}
}
