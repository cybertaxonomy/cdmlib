/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Transfer object derived from {@link LanguageString} excluding some {@link CdmBase} fields
 * 
 * Term descriptions are to be delivered by a separate web service request.
 * 
 * @author a.kohlbecker
 * @author  m.doering
 * @version 1.0
 * @created 11.12.2007 12:10:45
 *
 */
public class LocalisedTermTO {

	private String term;
	private String uuid;
	private String language;
	
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
}
