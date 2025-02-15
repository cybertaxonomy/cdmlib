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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbStringMapper
        extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {

    private static final Logger logger = LogManager.getLogger();

	private static final int MAX_PRECISION = -1;  //precision for datatype nvarchar(max) == clob (SQL Server 2008)
	private boolean blankToNull = false;

	public static DbStringMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbStringMapper(cdmAttributeString, dbAttributeString, null, true);
	}

	/**
     * Returns a string mapper which does not require that the source attribute exists.
     *
     * @param cdmAttributeString source attribute (CDM)
     * @param dbAttributString target attribute (export DB)
     * @param defaultValue default value if source value is <code>null</code>
     * @param obligatory if the source attribute is obligatory, but value may be <code>null</code>
     */
	public static DbStringMapper NewFacultativeInstance(String cdmAttributeString, String dbAttributeString){
		return new DbStringMapper(cdmAttributeString, dbAttributeString, null, false);
	}

	/**
     * @param cdmAttributeString source attribute (CDM)
     * @param dbAttributString target attribute (export DB)
     * @param defaultValue default value if source value is <code>null</code>
     * @param obligatory if the source attribute is obligatory, but value may be <code>null</code>
     */
	public static DbStringMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue){
		return new DbStringMapper(cdmAttributeString, dbAttributeString, defaultValue, false);
	}

	/**
     * @param cdmAttributeString source attribute (CDM)
     * @param dbAttributString target attribute (export DB)
     * @param defaultValue default value if source value is <code>null</code>
     * @param obligatory if the source attribute is obligatory, but value may be <code>null</code>
     */
	public static DbStringMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory){
		return new DbStringMapper(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}

	private DbStringMapper(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory) {
		super(cdmAttributeString, dbAttributeString, defaultValue, obligatory, false);
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		String result = (String)super.getValue(cdmBase);
		if (isBlankToNull() && StringUtils.isBlank(result)){
			result = null;
		}
		if (result != null){
			if (result.startsWith(" ") || result.endsWith(" ")){
				result = result.trim();
			}
			//truncate if needed
			if (result.length() > getPrecision() && getPrecision() != MAX_PRECISION && getPrecision() > 0){
				logger.warn("Truncation (" + result.length() + "->" + getPrecision() + ") needed for Attribute " + getDestinationAttribute() + " in " +  cdmBase + "." );
				result = result.substring(0, getPrecision());
			}
		}
		return result;
	}


	/**
	 * If <code>true</code> all {@link DbStringMapper} map blank strings to <code>null</code>
	 * @return
	 */
	public boolean isBlankToNull() {
		return blankToNull;
	}
	/**
	 * @see #isBlankToNull()
	 * @param blankToNull
	 */
	public DbStringMapper setBlankToNull(boolean blankToNull) {
		this.blankToNull = blankToNull;
		return this;
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
