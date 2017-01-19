/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.model.description.TextData;

/**
 * @author a.kohlbecker
 * @date 13.10.2010
 *
 */
public interface INaturalLanguageTextDataProcessor {

	/**
	 * Applies some special processing to the text contained in the TextData or/and
	 * to the Feature label/representation
	 * 
	 * @param textData
	 * @param previousTextData TODO
	 */
	public void process(TextData textData, TextData previousTextData);
	
}
