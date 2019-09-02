/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 24.02.2010
 */
public class DbImportTimePeriodMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportTimePeriodMapper.class);

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
	protected CdmBase doInvoke(CdmBase cdmBase, Object value) throws SQLException {
		if (value != null && ! (value instanceof TimePeriod) ){
		    if (isVerbatim){
                value = TimePeriodParser.parseStringVerbatim(String.valueOf(value));
		    }else{
		        value = TimePeriodParser.parseString(String.valueOf(value));
		    }
		}
		return super.doInvoke(cdmBase, value);
	}

	@Override
	public Class getTypeClass() {
		return TimePeriod.class;
	}
}
