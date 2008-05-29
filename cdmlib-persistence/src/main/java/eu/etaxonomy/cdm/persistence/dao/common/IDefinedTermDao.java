/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;


public interface IDefinedTermDao extends ICdmEntityDao<DefinedTermBase>, ITitledDao<DefinedTermBase>{
	
	/**
	 * @param iso639 a two or three letter language code according to iso639-1 or iso639-2
	 * @return the Language or null
	 */
	public Language getLangaugeByIso(String iso639);

	
}
