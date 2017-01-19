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
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbTimePeriodMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbTimePeriodMapper.class);

	public static DbTimePeriodMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbTimePeriodMapper(cdmAttributeString, dbAttributeString, null, true);
	}

	public static DbTimePeriodMapper NewFacultativeInstance(String cdmAttributeString, String dbAttributeString){
		return new DbTimePeriodMapper(cdmAttributeString, dbAttributeString, null, false);
	}

	public static DbTimePeriodMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue){
		return new DbTimePeriodMapper(cdmAttributeString, dbAttributeString, defaultValue, false);
	}

	public static DbTimePeriodMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory){
		return new DbTimePeriodMapper(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	private DbTimePeriodMapper(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory) {
		super(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		String result = null;
		TimePeriod timePeriod = (TimePeriod)super.getValue(cdmBase);
		if (timePeriod != null){
			result = timePeriod.toString();
		}
		return result;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}

}
