/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This mapper does not change any import object, but keeps the information that a database
 * attribute needs to be mapped but is not yet mapped.
 *
 * @see DbExportIgnoreMapper
 * @see DbNullMapper
 *
 * @author a.mueller
 * @since 25.02.2010
 */
public class DbExportNotYetImplementedMapper
        extends DbExportIgnoreMapper {

    private static final Logger logger = Logger.getLogger(DbExportNotYetImplementedMapper.class);

	public static DbExportNotYetImplementedMapper NewInstance(String dbAttributeToIgnore){
		return new DbExportNotYetImplementedMapper(null, dbAttributeToIgnore, null, null);
	}

	public static DbExportNotYetImplementedMapper NewInstance(String dbAttributeToIgnore, String reason){
		return new DbExportNotYetImplementedMapper(null, dbAttributeToIgnore, null, reason);
	}

//*************************** CONSTRUCTOR ***************************************************************//

	protected DbExportNotYetImplementedMapper(String cdmAttributeString, String dbAttributString, Object defaultValue, String unimplementedReason) {
		super(cdmAttributeString, dbAttributString, defaultValue, unimplementedReason);
	}

	@Override
    protected void initializeLogging() {
	    String attributeName = this.dbAttributeString;
        String localReason = "";
        if (StringUtils.isNotBlank(ignoreReason)){
            localReason = " (" + ignoreReason +")";
        }
        logger.warn(attributeName + " not yet implemented." +  localReason);
	}
}
