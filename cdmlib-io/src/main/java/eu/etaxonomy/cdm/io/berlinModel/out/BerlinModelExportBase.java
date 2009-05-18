/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.out;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public abstract class BerlinModelExportBase<T extends CdmBase> extends CdmIoBase<IExportConfigurator> implements ICdmIO<IExportConfigurator> {
	private static final Logger logger = Logger.getLogger(BerlinModelExportBase.class);
	
	public BerlinModelExportBase() {
		super();
	}
	
	protected abstract boolean doInvoke(BerlinModelExportState<BerlinModelExportConfigurator> state);
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IExportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){ 
		BerlinModelExportState<BerlinModelExportConfigurator> state = ((BerlinModelExportConfigurator)config).getState();
		state.setConfig((BerlinModelExportConfigurator)config);
		return doInvoke(state);
	}
	
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
