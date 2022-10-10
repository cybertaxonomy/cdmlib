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

import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.exception.CannotDisseminateFormatException;

public class MetadataPrefixEditor extends PropertyEditorSupport {

	@Override
    public void setAsText(String text) {
		if(text == null) {
			throw new IllegalArgumentException("null is not an acceptable metadata format");
		} else {
			MetadataPrefix metadatPrefix = MetadataPrefix.value(text);
			if(metadatPrefix  != null){
				setValue(metadatPrefix);

//			if(text.equals("rdf")) {
//				setValue(MetadataPrefix.RDF);
//			} else if(text.equals("oai_dc")) {
//				setValue(MetadataPrefix.OAI_DC);
			} else {
				throw new CannotDisseminateFormatException(text + " is not an acceptable metadata format");
			}
		}
	}

	@Override
    public String getAsText() {
		if(getValue() == null) {
			return null;
		} else {
		    return ((MetadataPrefix)getValue()).name();
		}
	}
}