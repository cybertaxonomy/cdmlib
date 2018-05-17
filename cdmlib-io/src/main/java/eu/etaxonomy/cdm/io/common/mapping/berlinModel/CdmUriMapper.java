/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.berlinModel;

import java.net.URI;

import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;

/**
 * @author a.mueller
 * @since 20.03.2008
 * @version 1.0
 */
public class CdmUriMapper extends CdmSingleAttributeMapperBase {

	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	public CdmUriMapper(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}

	public Class getTypeClass(){
		return URI.class;
	}
}
