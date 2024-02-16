/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.io.Serializable;


public class DerivedUnitStatusDto implements Serializable{

	private static final long serialVersionUID = 6463365950608923394L;

	private SourceDTO statusSource;
	private String label;

	public DerivedUnitStatusDto(String label) {
		this.setLabel(label);

	}

	public SourceDTO getStatusSource() {
		return statusSource;
	}
	public void setStatusSource(SourceDTO statusSource) {
		this.statusSource = statusSource;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}