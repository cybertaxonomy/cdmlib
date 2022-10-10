/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.mapping.berlinModel;

import java.util.UUID;

import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;

/**
 * @author a.mueller
 * @since 20.09.2022
 */
public class CdmUuidMapper extends CdmSingleAttributeMapperBase {

	public CdmUuidMapper(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}

	@Override
    public Class getTypeClass(){
		return UUID.class;
	}
}
