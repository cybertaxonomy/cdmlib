package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.exception.CannotDisseminateFormatException;

public class MetadataPrefixEditor extends PropertyEditorSupport {
	
	public void setAsText(String text) {
		if(text == null) {
			throw new IllegalArgumentException("null is not an acceptable metadata format");
		} else {
			if(text.equals("rdf")) {
				setValue(MetadataPrefix.RDF);
			} else if(text.equals("oai_dc")) {
				setValue(MetadataPrefix.OAI_DC);
			} else {
				throw new CannotDisseminateFormatException(text + " is not an acceptable metadata format");
	}
		}
	}
	
	public String getAsText() {
		if(getValue() == null) {
			return null;
		} else {
		    return ((MetadataPrefix)getValue()).name();
	}
	}
}
