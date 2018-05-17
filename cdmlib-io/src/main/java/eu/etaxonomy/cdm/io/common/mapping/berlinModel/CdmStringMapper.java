/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;

/**
 * @author a.mueller
 * @since 20.03.2008
 * @version 1.0
 */
public class CdmStringMapper extends CdmSingleAttributeMapperBase {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CdmStringMapper.class);
	
	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmStringMapper(String dbAttributeString, String cdmAttributeString) {
		super(dbAttributeString, cdmAttributeString);
	}
	
	public Class<String> getTypeClass(){
		return String.class;
	}
	
}
