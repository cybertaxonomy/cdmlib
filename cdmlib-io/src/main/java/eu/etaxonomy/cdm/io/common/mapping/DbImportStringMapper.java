// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 24.02.2010
 * @version 1.0
 */
public class DbImportStringMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase, CdmBase>{
	private static final Logger logger = Logger.getLogger(DbImportStringMapper.class);
	
	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	public static DbImportStringMapper NewInstance (String dbAttributString, String cdmAttributeString, Object defaultValue) {
		boolean obligatory = false;
		return new  DbImportStringMapper(dbAttributString, cdmAttributeString, defaultValue, obligatory);
	}

	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	public static DbImportStringMapper NewInstance (String dbAttributString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		return new  DbImportStringMapper(dbAttributString, cdmAttributeString, defaultValue, obligatory);
	}

	
	
	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	protected DbImportStringMapper(String dbAttributString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		super(cdmAttributeString, dbAttributString, defaultValue, obligatory);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class getTypeClass() {
		// TODO Auto-generated method stub
		return null;
	}
}
