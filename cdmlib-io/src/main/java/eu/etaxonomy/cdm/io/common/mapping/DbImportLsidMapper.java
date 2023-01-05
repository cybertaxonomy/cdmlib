/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.LSID;

/**
 * @author a.mueller
 * @since 24.02.2010
 */
public class DbImportLsidMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase>{

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public static DbImportLsidMapper NewInstance (String dbAttributeString, String cdmAttributeString) {
		boolean obligatory = false;
		Object defaultValue = null;
		return DbImportLsidMapper.NewInstance(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	public static DbImportLsidMapper NewInstance (String dbAttributString, String cdmAttributeString, Object defaultValue) {
		boolean obligatory = false;
		return new  DbImportLsidMapper(dbAttributString, cdmAttributeString, defaultValue, obligatory);
	}

	public static DbImportLsidMapper NewInstance (String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		return new  DbImportLsidMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	protected DbImportLsidMapper(String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		super(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	@Override
	protected CdmBase doInvoke(CdmBase cdmBase, Object value) {
		if (value != null && ! (value instanceof LSID) ){
			try {
				value = new LSID(String.valueOf(value));
			} catch (MalformedLSIDException e) {
				throw new RuntimeException(String.format("LSID %s is malformed", value), e);
			}
		}
		return super.doInvoke(cdmBase, value);
	}

	@Override
	public Class getTypeClass() {
		return LSID.class;
	}
}
