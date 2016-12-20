/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Maps to a constant value.
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbConstantMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbConstantMapper.class);
	private int sqlType;
	private Object value;
	
	public static DbConstantMapper NewInstance(String dbIdAttributeString, int sqlType, Object value){
		return new DbConstantMapper(dbIdAttributeString, sqlType, value);
	}
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	protected DbConstantMapper(String dbAttributeString, int sqlType, Object value) {
		super(null, dbAttributeString, null);
		this.sqlType = sqlType;
		this.value = value;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue()
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		return value;
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return this.sqlType;
	}
	
}
