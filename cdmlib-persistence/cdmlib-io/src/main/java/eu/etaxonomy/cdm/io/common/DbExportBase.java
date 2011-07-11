// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 17.02.2010
 *
 */
public abstract class DbExportBase<CONFIG extends IExportConfigurator, STATE extends ExportStateBase> extends CdmExportBase<CONFIG, STATE> {
	private static Logger logger = Logger.getLogger(DbExportBase.class);

//	protected abstract boolean doInvoke(BerlinModelExportState<BerlinModelExportConfigurator> state);
	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
//	 */
//	protected boolean doInvoke(BerlinModelExportConfigurator config, 
//			Map<String, MapWrapper<? extends CdmBase>> stores){ 
//		BerlinModelExportState state = ((BerlinModelExportConfigurator)config).getState();
//		state.setConfig((BerlinModelExportConfigurator)config);
//		return doInvoke(state);
//	}
	
	protected boolean checkSqlServerColumnExists(Source source, String tableName, String columnName){
		String strQuery = "SELECT  Count(t.id) as n " +
				" FROM sysobjects AS t " +
				" INNER JOIN syscolumns AS c ON t.id = c.id " +
				" WHERE (t.xtype = 'U') AND " + 
				" (t.name = '" + tableName + "') AND " + 
				" (c.name = '" + columnName + "')";
		ResultSet rs = source.getResultSet(strQuery) ;		
		int n;
		try {
			rs.next();
			n = rs.getInt("n");
			return n>0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public abstract Class<? extends CdmBase> getStandardMethodParameter();
	
	protected void doCount(int count, int modCount, String pluralString){
		if ((count % modCount ) == 0 && count!= 0 ){ logger.info(pluralString + " handled: " + (count));}
	}
}
