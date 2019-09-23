/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 24.02.2010
 */
public class DbImportStringMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportStringMapper.class);

	public static DbImportStringMapper NewInstance (String dbAttributeString, String cdmAttributeString) {
		boolean obligatory = false;
		Object defaultValue = null;
		return DbImportStringMapper.NewInstance(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	public static DbImportStringMapper NewInstance (String dbAttributString, String cdmAttributeString, Object defaultValue) {
		boolean obligatory = false;
		return new  DbImportStringMapper(dbAttributString, cdmAttributeString, defaultValue, obligatory);
	}

	public static DbImportStringMapper NewInstance (String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		return new  DbImportStringMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	protected DbImportStringMapper(String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		super(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
	}

	@Override
	protected CdmBase doInvoke(CdmBase cdmBase, Object value){
	    String str = StringUtils.isBlank((String)value)? null:((String)value).trim();
		return super.doInvoke(cdmBase, str);
	}

	@Override
	public Class getTypeClass() {
		return String.class;
	}

}
