/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.markup;

import java.io.InputStream;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 29.06.2011
 *
 */
public class MarkupInputStream  {

	/**
	 * for testing 
	 */
	InputStream stream;

	public CdmBase read(){
		return null;
	}

	public boolean isEndOfStream() {
		return false;
	}

}
