/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This mapper does not change any import object, but keeps the information that a database
 * attribute needs to be mapped but is not yet mapped.
 * @author a.mueller
 * @created 25.02.2010
 * @version 1.0
 */
public class DbExportNotYetImplementedMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {
	private static final Logger logger = Logger.getLogger(DbExportNotYetImplementedMapper.class);
	
	public static DbExportNotYetImplementedMapper NewInstance(String dbAttributeToIgnore){
		return new DbExportNotYetImplementedMapper(dbAttributeToIgnore, null, null, null);
	}

	public static DbExportNotYetImplementedMapper NewInstance(String dbAttributeToIgnore, String reason){
		return new DbExportNotYetImplementedMapper(dbAttributeToIgnore, null, null, reason);
	}

//*************************** VARIABLES ***************************************************************//
	private String unimplementedReason;
	
//*************************** CONSTRUCTOR ***************************************************************//
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 * @param defaultValue
	 */
	protected DbExportNotYetImplementedMapper(String dbAttributString, String cdmAttributeString, Object defaultValue, String unimplementedReason) {
		super(dbAttributString, cdmAttributeString, defaultValue);
		this.unimplementedReason = unimplementedReason;
	}

	@Override
	public void initialize(PreparedStatement stmt, IndexCounter index, DbExportStateBase state, String tableName) {
		String attributeName = this.getSourceAttribute();
		String localReason = "";
		if (StringUtils.isNotBlank(unimplementedReason)){
			localReason = " (" + unimplementedReason +")";
		}
		logger.warn(attributeName + " not yet implemented . " +  localReason);
		exportMapperHelper.initializeNull(stmt, state, tableName);
	}

	@Override
	protected boolean doInvoke(CdmBase cdmBase) throws SQLException {
		return true;   // do nothing
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class getTypeClass() {
		return null;  //not needed
	}
	

	@Override
	protected int getSqlType() {
		return -1;  // not needed
	}

	
	
	
}
