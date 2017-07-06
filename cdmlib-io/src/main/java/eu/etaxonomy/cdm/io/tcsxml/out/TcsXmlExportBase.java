/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsxml.out;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 20.03.2008
 */
public abstract class TcsXmlExportBase<T extends CdmBase>
        extends CdmExportBase<TcsXmlExportConfigurator, TcsXmlExportState, IExportTransformer, File>  {

    private static final long serialVersionUID = -7100332849797374995L;
    private static final Logger logger = Logger.getLogger(TcsXmlExportBase.class);

	public TcsXmlExportBase() {
		super();
	}

	@Override
    protected abstract void doInvoke(TcsXmlExportState state);

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
