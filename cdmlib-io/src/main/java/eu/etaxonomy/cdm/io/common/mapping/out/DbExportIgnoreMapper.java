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
 * @since 25.02.2010
 * @version 1.0
 */
public class DbExportIgnoreMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {
	private static final Logger logger = Logger.getLogger(DbExportIgnoreMapper.class);
	
	public static DbExportIgnoreMapper NewInstance(String dbAttributeToIgnore){
		return new DbExportIgnoreMapper(dbAttributeToIgnore, null, null, null);
	}

	public static DbExportIgnoreMapper NewInstance(String dbAttributeToIgnore, String reason){
		return new DbExportIgnoreMapper(dbAttributeToIgnore, null, null, reason);
	}

//*************************** VARIABLES ***************************************************************//
	private String ignoreReason;
	
//*************************** CONSTRUCTOR ***************************************************************//
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 * @param defaultValue
	 */
	protected DbExportIgnoreMapper(String dbAttributString, String cdmAttributeString, Object defaultValue, String ignoreReason) {
		super(dbAttributString, cdmAttributeString, defaultValue);
		this.ignoreReason = ignoreReason;
	}

	@Override
	public void initialize(PreparedStatement stmt, IndexCounter index, DbExportStateBase state, String tableName) {
		String attributeName = this.getSourceAttribute();
		String localReason = "";
		if (StringUtils.isNotBlank(ignoreReason)){
			localReason = " (" + ignoreReason +")";
		}
		logger.warn(attributeName + " ignored . " +  localReason);
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
