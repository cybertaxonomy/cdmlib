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

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Mapper to export DOIs to a DB text field.
 *
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbDoiMapper
            extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {

    private static final Logger logger = Logger.getLogger(DbDoiMapper.class);

    private static final int MAX_PRECISION = -1;  //precision for datatype nvarchar(max) == clob (SQL Server 2008)

    public static DbDoiMapper NewInstance(String cdmAttributeString, String dbAttributeString){
        return new DbDoiMapper(cdmAttributeString, dbAttributeString, null, true);
    }

    /**
     * @param cdmAttributeString source attribute (CDM)
     * @param dbAttributString target attribute (export DB)
     * @param defaultValue default value if source value is <code>null</code>
     * @param obligatory if the source attribute is obligatory, but value may be <code>null</code>
     */
    public static DbDoiMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue){
        return new DbDoiMapper(cdmAttributeString, dbAttributeString, defaultValue, false);
    }

    /**
     * @param cdmAttributeString source attribute (CDM)
     * @param dbAttributString target attribute (export DB)
     * @param defaultValue default value if source value is <code>null</code>
     * @param obligatory if the source attribute is obligatory, but value may be <code>null</code>
     */
    public static DbDoiMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory){
        return new DbDoiMapper(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
    }

    /**
     * @param cdmAttributeString source attribute (CDM)
     * @param dbAttributString target attribute (export DB)
     * @param defaultValue default value if source value is <code>null</code>
     * @param obligatory if the source attribute is obligatory, but value may be <code>null</code>
     */
    private DbDoiMapper(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory) {
        super(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
    }

    @Override
    protected Object getValue(CdmBase cdmBase) {
        DOI uri = (DOI)super.getValue(cdmBase);
        String result = (uri == null ? null : uri.toString());
        //Truncation handling copied from DbUriMapper
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
        return DOI.class;
    }
}
