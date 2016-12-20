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
 * This mapper removes objects from the stream of objects to be mapped.
 * This is a very perliminarye implementation. In future there will be at least an 
 * interface to implement and some more parameter to better define which values to include/exclude.
 * 
 * Current implementation
 * 
 * @author a.mueller
 * @created 13.02.2012
 */
public class DbSimpleFilterMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {
	private static final Logger logger = Logger.getLogger(DbSimpleFilterMapper.class);
	
	
	
	public static DbSimpleFilterMapper NewSingleNullAttributeInstance(String cdmFilterCriterionAttribute, String reason){
		return new DbSimpleFilterMapper(null, cdmFilterCriterionAttribute, null, reason);
	}

//*************************** VARIABLES ***************************************************************//
	private String filterReason;
	
//*************************** CONSTRUCTOR ***************************************************************//
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 * @param defaultValue
	 */
	protected DbSimpleFilterMapper(String dbAttributString, String cdmAttributeString, Object defaultValue, String filterReason) {
		super(cdmAttributeString, dbAttributString, defaultValue);
		this.filterReason = filterReason;
	}

	@Override
	public void initialize(PreparedStatement stmt, IndexCounter index, DbExportStateBase state, String tableName) {
		String attributeName = this.getDestinationAttribute();
		String localReason = "";
		if (StringUtils.isNotBlank(filterReason)){
			localReason = " (" + filterReason +")";
		}
		logger.info(" Some objects are filtered on " + attributeName + "." +  localReason);
		exportMapperHelper.initializeNull(stmt, state, tableName);
	}

	@Override
	protected boolean doInvoke(CdmBase cdmBase) throws SQLException {
		if (isFiltered(cdmBase)){
			return false;
		}else{
			return true; // do nothing
		}
	}
	

	private boolean isFiltered(CdmBase cdmBase) {
		Object value = super.getValue(cdmBase);
		return (value == null);
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
