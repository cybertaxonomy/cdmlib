/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbDateMapper extends DbSingleAttributeExportMapperBase implements IDbExportMapper {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	public static DbDateMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbDateMapper(cdmAttributeString, dbAttributeString, null);
	}

	public static DbDateMapper NewInstance(String cdmAttributeString, String dbAttributeString, DateTime defaultValue){
		return new DbDateMapper(cdmAttributeString, dbAttributeString, defaultValue);
	}

	private DbDateMapper(String cdmAttributeString, String dbAttributeString, DateTime defaultValue) {
		super(cdmAttributeString, dbAttributeString, defaultValue);
	}

//	@Override
//	protected boolean doInvoke(CdmBase cdmBase) throws SQLException{
//		boolean obligat = true;
//		try {
//			DateTime value = (DateTime)ImportHelper.getValue(
//					cdmBase, this.getSourceAttribute(), this.getTypeClass(), false, obligat);
//			if (value == null){
//				getPreparedStatement().setNull(getIndex(), Types.DATE);
//			}else{
//				java.util.Date date = value.toDate();
//				long t = date.getTime();
//				java.sql.Date sqlDate = new java.sql.Date(t);
//				getPreparedStatement().setDate(getIndex(), sqlDate);
//			}
//			return true;
//		} catch (SQLException e) {
//			logger.warn("SQL Exception: " + e.getLocalizedMessage());
//			throw e;
//		}
//	}

//	@Override
//	protected Object getValue(CdmBase cdmBase) {
//		boolean obligat = true;
//		return (DateTime)ImportHelper.getValue(cdmBase, this.getSourceAttribute(), this.getTypeClass(), false, obligat);
//	}



	@Override
	protected int getSqlType() {
		return Types.DATE;
	}

	@Override
	public Class<?> getTypeClass() {
		return DateTime.class;
	}
}