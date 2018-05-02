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


/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public class DbBooleanMapper extends DbSingleAttributeExportMapperBase implements IDbExportMapper {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbBooleanMapper.class);

	public static DbBooleanMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbBooleanMapper(cdmAttributeString, dbAttributeString, null, false);
	}

	public static DbBooleanMapper NewInstance(String cdmAttributeString, String dbAttributeString, Boolean defaultValue){
		return new DbBooleanMapper(cdmAttributeString, dbAttributeString, defaultValue, false);
	}

	public static DbBooleanMapper NewFalseInstance(String cdmAttributeString, String dbAttributeString){
		return new DbBooleanMapper(cdmAttributeString, dbAttributeString, false, false);
	}

	public static DbBooleanMapper NewInstance(String cdmAttributeString, String dbAttributeString, Boolean defaultValue, Boolean obligatory){
		return new DbBooleanMapper(cdmAttributeString, dbAttributeString, false, obligatory);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	private DbBooleanMapper(String cdmAttributeString, String dbAttributeString, Boolean defaultValue, Boolean obligatory) {
		super(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.BOOLEAN;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return Boolean.class;
	}



}
