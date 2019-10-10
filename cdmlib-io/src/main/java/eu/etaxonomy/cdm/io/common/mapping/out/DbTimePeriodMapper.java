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
 * @since 12.05.2009
 */
public class DbTimePeriodMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbTimePeriodMapper.class);

	public static DbTimePeriodMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbTimePeriodMapper(cdmAttributeString, dbAttributeString, null, true);
	}

	public static DbTimePeriodMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue){
		return new DbTimePeriodMapper(cdmAttributeString, dbAttributeString, defaultValue, false);
	}

	/**
     * @param cdmAttributeString source attribute (CDM)
     * @param dbAttributString target attribute (export DB)
     * @param defaultValue default value if source value is <code>null</code>
     * @param obligatory if the source attribute is obligatory, but value may be <code>null</code>
     */
	public static DbTimePeriodMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory){
		return new DbTimePeriodMapper(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}

	/**
     * @param cdmAttributeString source attribute (CDM)
     * @param dbAttributString target attribute (export DB)
     * @param defaultValue default value if source value is <code>null</code>
     * @param obligatory if the source attribute is obligatory, but value may be <code>null</code>
     */
	private DbTimePeriodMapper(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory) {
		super(cdmAttributeString, dbAttributeString, defaultValue, obligatory, false);
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		String result = null;
		TimePeriod timePeriod = (TimePeriod)super.getValue(cdmBase);
		if (timePeriod != null){
			result = timePeriod.toString();
		}
		return result;
	}

	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	public Class<?> getTypeClass() {
		return TimePeriod.class;
	}
}
