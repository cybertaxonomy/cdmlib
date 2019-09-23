/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 03.09.2019
 */
public class DbImportDoiMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportDoiMapper.class);

	public static DbImportDoiMapper NewInstance (String dbAttributeString, String cdmAttributeString) {
		boolean obligatory = false;
		Object defaultValue = null;
		return new DbImportDoiMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	public static DbImportDoiMapper NewInstance (String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		return new  DbImportDoiMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	protected DbImportDoiMapper(String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		super(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	@Override
	protected CdmBase doInvoke(CdmBase cdmBase, Object value) {
		if (value != null && ! (value instanceof DOI) ){
		    value = DOI.fromString(String.valueOf(value));
		}
		return super.doInvoke(cdmBase, value);
	}

	@Override
	public Class getTypeClass() {
		return DOI.class;
	}
}
