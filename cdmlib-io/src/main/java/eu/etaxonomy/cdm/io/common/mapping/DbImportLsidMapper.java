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
import org.apache.log4j.Logger;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.LSID;

/**
 * @author a.mueller
 * @created 24.02.2010
 * @version 1.0
 */
public class DbImportLsidMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportLsidMapper.class);
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 * @return
	 */
	public static DbImportLsidMapper NewInstance (String dbAttributeString, String cdmAttributeString) {
		boolean obligatory = false;
		Object defaultValue = null;
		return DbImportLsidMapper.NewInstance(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}
	
	
	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	public static DbImportLsidMapper NewInstance (String dbAttributString, String cdmAttributeString, Object defaultValue) {
		boolean obligatory = false;
		return new  DbImportLsidMapper(dbAttributString, cdmAttributeString, defaultValue, obligatory);
	}

	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	public static DbImportLsidMapper NewInstance (String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		return new  DbImportLsidMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	
	
	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	protected DbImportLsidMapper(String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		super(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#doInvoke(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected CdmBase doInvoke(CdmBase cdmBase, Object value) throws SQLException {
		if (value != null && ! (value instanceof LSID) ){
			try {
				value = new LSID(String.valueOf(value));
			} catch (MalformedLSIDException e) {
				throw new RuntimeException(String.format("LSID %s is malformed", value), e);
			}
		}
		return super.doInvoke(cdmBase, value);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class getTypeClass() {
		return LSID.class;
	}
	

}
