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
 * This mapper does not change any import object, but logs the information that a database
 * attribute is ignored for the mapping.
 *
 * @see DbNullMapper
 *
 * @author a.mueller
 * @since 25.02.2010
 */
public class DbExportIgnoreMapper
            extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {

    private static final Logger logger = Logger.getLogger(DbExportIgnoreMapper.class);

	public static DbExportIgnoreMapper NewInstance(String dbAttributeToIgnore){
		return new DbExportIgnoreMapper(null, dbAttributeToIgnore, null, null);
	}

	public static DbExportIgnoreMapper NewInstance(String dbAttributeToIgnore, String reason){
		return new DbExportIgnoreMapper(null, dbAttributeToIgnore, null, reason);
	}

//*************************** VARIABLES ***************************************************************//

	protected String ignoreReason;
	protected String dbAttributeString;

//*************************** CONSTRUCTOR ***************************************************************//

	protected DbExportIgnoreMapper(String cdmAttributeString, String dbAttributeString, Object defaultValue, String ignoreReason) {
		super(cdmAttributeString, null, defaultValue);
		//we do not pass to parent, otherwise this is part of the prepared statement
		//once we check if all fields of a table are mapped this behavior needs to be changed
		//or at least the checking mechanism needs to know that this attribute is handled (but ignored)
		this.dbAttributeString = dbAttributeString;
		this.ignoreReason = ignoreReason;
	}

	@Override
	public void initialize(PreparedStatement stmt, IndexCounter index, DbExportStateBase state, String tableName) {
		initializeLogging();
		exportMapperHelper.initializeNull(stmt, state, tableName);
	}

    protected void initializeLogging() {
        String attributeName = dbAttributeString; //this.getDestinationAttribute();
        String localReason = "";
        if (StringUtils.isNotBlank(ignoreReason)){
            localReason = " (" + ignoreReason +")";
        }
        logger.warn(attributeName + " ignored" +  localReason + ".");
    }

    @Override
	protected boolean doInvoke(CdmBase cdmBase) throws SQLException {
		return true;   // do nothing
	}

	@Override
	public Class getTypeClass() {
		return null;  //not needed
	}

	@Override
	protected int getSqlType() {
		return -1;  // not needed
	}
}
