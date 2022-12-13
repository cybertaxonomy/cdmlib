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

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 02.09.2019
 */
public class DbImportTimePeriodMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase>{

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	boolean isVerbatim = false;

	public static DbImportTimePeriodMapper NewInstance (String dbAttributeString, String cdmAttributeString) {
		boolean obligatory = false;
		Object defaultValue = null;
		boolean isVerbatim = false;
		return new DbImportTimePeriodMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory, isVerbatim);
	}

    public static DbImportTimePeriodMapper NewVerbatimInstance (String dbAttributeString, String cdmAttributeString) {
        boolean obligatory = false;
        Object defaultValue = null;
        boolean isVerbatim = true;
        return new DbImportTimePeriodMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory, isVerbatim);
    }

	public static DbImportTimePeriodMapper NewInstance (String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory, boolean isVerbatim) {
		return new  DbImportTimePeriodMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory, isVerbatim);
	}

	protected DbImportTimePeriodMapper(String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory, boolean isVerbatim) {
		super(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
		this.isVerbatim = isVerbatim;
	}

	@Override
	protected CdmBase doInvoke(CdmBase cdmBase, Object value){
		if (value != null && ! (value instanceof TimePeriod) ){
		    String strValue = String.valueOf(value).trim();
		    if (isVerbatim){
                value = TimePeriodParser.parseStringVerbatim(strValue);
		    }else{
		        value = TimePeriodParser.parseString(strValue);
		    }
		}
		return super.doInvoke(cdmBase, value);
	}

	@Override
	public Class getTypeClass() {
		return TimePeriod.class;
	}
}