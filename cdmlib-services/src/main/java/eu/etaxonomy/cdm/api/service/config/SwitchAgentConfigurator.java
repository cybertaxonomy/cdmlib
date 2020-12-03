/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

public class SwitchAgentConfigurator {

	private boolean doAddPersonAsMember = true;

	public boolean isDoAddPersonAsMember() {
		return doAddPersonAsMember;
	}

	public void setDoAddPersonAsMember(boolean doAddPersonAsMember) {
		this.doAddPersonAsMember = doAddPersonAsMember;
	}
}
