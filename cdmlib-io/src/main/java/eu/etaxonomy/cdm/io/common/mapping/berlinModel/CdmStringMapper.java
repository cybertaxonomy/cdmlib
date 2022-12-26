/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.berlinModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;

/**
 * @author a.mueller
 * @since 20.03.2008
 */
public class CdmStringMapper extends CdmSingleAttributeMapperBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	public CdmStringMapper(String dbAttributeString, String cdmAttributeString) {
		super(dbAttributeString, cdmAttributeString);
	}

	@Override
    public Class<String> getTypeClass(){
		return String.class;
	}
}