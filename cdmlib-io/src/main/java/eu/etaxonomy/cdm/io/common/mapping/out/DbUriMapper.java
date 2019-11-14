/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.net.URI;
import java.sql.Types;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbUriMapper
            extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {

    private static final Logger logger = Logger.getLogger(DbUriMapper.class);

	private static final int MAX_PRECISION = -1;  //precision for datatype nvarchar(max) == clob (SQL Server 2008)

	public static DbUriMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbUriMapper(cdmAttributeString, dbAttributeString, null, true);
	}

	public static DbUriMapper NewFacultativeInstance(String cdmAttributeString, String dbAttributeString){
		return new DbUriMapper(cdmAttributeString, dbAttributeString, null, false);
	}

	public static DbUriMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue){
		return new DbUriMapper(cdmAttributeString, dbAttributeString, defaultValue, false);
	}

	public static DbUriMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory){
		return new DbUriMapper(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}

	private DbUriMapper(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory) {
		super(cdmAttributeString, dbAttributeString, defaultValue, obligatory, false);
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		URI uri = (URI)super.getValue(cdmBase);
		String result = (uri == null ? null : uri.toString());
		if (result !=null && result.length() > getPrecision() && getPrecision() != MAX_PRECISION){
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
		return URI.class;
	}
}
