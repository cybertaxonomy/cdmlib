/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;

public class SetSpecEditor extends PropertyEditorSupport {

	@Override
    public void setAsText(String text) {
		if(text == null) {
			throw new IllegalArgumentException("null is not an acceptable set spec");
		} else {
			SetSpec setSpec = SetSpec.bySpec(text);
			if(setSpec != null){
				setValue(setSpec);
			} else {
				throw new IllegalArgumentException(text + " is not an acceptable set spec");
			}
		}
	}

	@Override
    public String getAsText() {
		if(getValue() == null) {
			return null;
		} else {
		    return ((SetSpec)getValue()).getSpec();
		}
	}
}
