/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.Types;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public class DbClobMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbClobMapper.class);

	public static DbClobMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbClobMapper(cdmAttributeString, dbAttributeString, null);
	}

	public static DbClobMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue){
		return new DbClobMapper(cdmAttributeString, dbAttributeString, defaultValue);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	private DbClobMapper(String cdmAttributeString, String dbAttributeString, String defaultValue) {
		super(cdmAttributeString, dbAttributeString, defaultValue);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.CLOB;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}



}
