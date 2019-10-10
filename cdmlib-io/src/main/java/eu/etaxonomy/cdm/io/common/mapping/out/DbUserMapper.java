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
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbUserMapper
            extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbUserMapper.class);

	public static DbUserMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbUserMapper(cdmAttributeString, dbAttributeString, null, true);
	}

	public static DbUserMapper NewFacultativeInstance(String cdmAttributeString, String dbAttributeString){
		return new DbUserMapper(cdmAttributeString, dbAttributeString, null, false);
	}

	public static DbUserMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue){
		return new DbUserMapper(cdmAttributeString, dbAttributeString, defaultValue, false);
	}

	public static DbUserMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory){
		return new DbUserMapper(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}

	private DbUserMapper(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory) {
		super(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		String result = null;
		User user = (User)super.getValue(cdmBase);
		if (user != null){
			result = user.getUsername();
		}
		return result;
	}

	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}
}
