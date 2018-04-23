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

import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase.IdType;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @since 17.02.2010
 *
 */
public abstract class DbExportBase<CONFIG extends DbExportConfiguratorBase<STATE, TRANSFORM, Source>, STATE extends DbExportStateBase<CONFIG, TRANSFORM>, TRANSFORM extends IExportTransformer>
            extends CdmExportBase<CONFIG, STATE, TRANSFORM, Source> {

    private static final long serialVersionUID = -1652695446752713850L;
    private static Logger logger = Logger.getLogger(DbExportBase.class);

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

	@Override
	public Object getDbId(CdmBase cdmBase, STATE state) {
		CONFIG config = state.getConfig();
		IdType type = config.getIdType();
		if (cdmBase == null){
			return null;
		}
		if (type == IdType.CDM_ID){
			return cdmBase.getId();
		}else if (type == IdType.CDM_ID_WITH_EXCEPTIONS){
				return getDbIdCdmWithExceptions(cdmBase, state);
		}else if(type == IdType.MAX_ID){
			//TODO
			logger.warn("MAX_ID not yet implemented");
			return cdmBase.getId();
		}else if(type == IdType.ORIGINAL_SOURCE_ID){
			//TODO
			logger.warn("ORIGINAL_SOURCE_ID not yet implemented");
			return cdmBase.getId();
		}else{
			logger.warn("Unknown idType: " + type);
			return cdmBase.getId();
		}

	}

	protected Object getDbIdCdmWithExceptions(CdmBase cdmBase, STATE state) {
		//default -> override
		return cdmBase.getId();
	}



}
