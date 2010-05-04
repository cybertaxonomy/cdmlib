package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;

public class SetSpecEditor extends PropertyEditorSupport {
	
	public void setAsText(String text) {
		if(text == null) {
			throw new IllegalArgumentException("null is not an acceptable set spec");
		} else {
			boolean matched = false;
			for(SetSpec setSpec : SetSpec.values()) {
				if(setSpec.name().equals(text)) {
					setValue(setSpec);
					break;
				}
			}
			if(!matched) {
			    throw new IllegalArgumentException(text + " is not an acceptable set spec");
			}
		}
	}
	
	public String getAsText() {
		if(getValue() == null) {
			return null;
		} else {
		    return ((SetSpec)getValue()).name();
		}
	}
}
