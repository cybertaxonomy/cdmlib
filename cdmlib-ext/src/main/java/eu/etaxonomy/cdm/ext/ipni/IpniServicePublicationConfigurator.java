/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.ipni;

/**
 * @author a.mueller
 */
public class IpniServicePublicationConfigurator extends IpniServiceConfiguratorBase {

	/**
	 * If true the abbreviation is used as title instead of the title
	 */
	private boolean useAbbreviationAsTitle = false;

	public void setUseAbbreviationAsTitle(boolean useAbbreviationAsTitle) {
		this.useAbbreviationAsTitle = useAbbreviationAsTitle;
	}

	public boolean isUseAbbreviationAsTitle() {
		return useAbbreviationAsTitle;
	}

}
