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

/**
 * @author a.mueller
 * @since 28.09.2019
 */
public class DbFixedStringMapper
        extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>>
        implements IDbExportMapper<DbExportStateBase<?,IExportTransformer>, IExportTransformer> {

    private static final Logger logger = Logger.getLogger(DbFixedStringMapper.class);

	private static final int MAX_PRECISION = -1;  //precision for datatype nvarchar(max) == clob (SQL Server 2008)

	private String fixString;

	public static DbFixedStringMapper NewInstance(String fixString, String dbAttributeString){
		return new DbFixedStringMapper(fixString, dbAttributeString);
	}

	private DbFixedStringMapper(String fixString, String dbAttributeString) {
		super(null, dbAttributeString, fixString, true, false);
		this.fixString = fixString;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		String result = fixString;

    	//truncate if needed
    	if (result != null && result.length() > getPrecision() && getPrecision() != MAX_PRECISION && getPrecision() > 0){
    		logger.warn("Truncation (" + result.length() + "->" + getPrecision() + ") needed for Attribute " + getDestinationAttribute() + " in " +  cdmBase + "." );
    		result = result.substring(0, getPrecision());
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
