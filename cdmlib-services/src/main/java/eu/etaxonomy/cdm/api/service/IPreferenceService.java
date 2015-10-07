/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 
package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.metadata.CdmPreference;

/**
 * @author a.mueller
 * @created 2013-09-09
 */
public interface IPreferenceService {

	/**
	 * Retrieve the best matching value for the given preference key.
	 * @param key
	 * @return
	 */
	public CdmPreference get(CdmPreference.PrefKey key);

	/**
	 * Write the value for the preference's key
	 * @param preference
	 */
	public void set(CdmPreference preference);
	
	/**
	 * Return the number of all existing cdm preferences
	 * @return
	 */
	public int count();

}