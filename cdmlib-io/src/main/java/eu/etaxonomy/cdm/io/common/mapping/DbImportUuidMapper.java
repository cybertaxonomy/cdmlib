/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 24.02.2010
 * @version 1.0
 */
public class DbImportUuidMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportUuidMapper.class);
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 * @return
	 */
	public static DbImportUuidMapper NewInstance (String dbAttributeString, String cdmAttributeString) {
		boolean obligatory = false;
		Object defaultValue = null;
		return DbImportUuidMapper.NewInstance(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}
	
	
	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	public static DbImportUuidMapper NewInstance (String dbAttributString, String cdmAttributeString, Object defaultValue) {
		boolean obligatory = false;
		return new  DbImportUuidMapper(dbAttributString, cdmAttributeString, defaultValue, obligatory);
	}

	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	public static DbImportUuidMapper NewInstance (String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		return new  DbImportUuidMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	
	
	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	protected DbImportUuidMapper(String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		super(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#doInvoke(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected CdmBase doInvoke(CdmBase cdmBase, Object value) throws SQLException {
		if (value != null && ! (value instanceof UUID) ){
			value = UUID.fromString(String.valueOf(value));
		}
		return super.doInvoke(cdmBase, value);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class getTypeClass() {
		return UUID.class;
	}
	

}
