// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.out;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IndexCounter;
import eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.CdmIoMapping;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class BerlinModelExportMapping extends CdmIoMapping {
	private static final Logger logger = Logger.getLogger(BerlinModelExportMapping.class);
	
	private PreparedStatement preparedStatement;
	private String berlinModelTableName;
	private boolean doExecute = true;
	
	public BerlinModelExportMapping(String tableName){
		this(tableName, true);
	}

	public BerlinModelExportMapping(String tableName, boolean doExecute){
		this.berlinModelTableName = tableName;
		this.doExecute = doExecute;
	}
	
	public boolean initialize(BerlinModelExportState<?> state) throws SQLException{
		return this.initialize(state, null);
	}
	
	public boolean initialize(BerlinModelExportState<?> state, PreparedStatement stmt) throws SQLException{
		BerlinModelExportConfigurator bmeConfig = (BerlinModelExportConfigurator)state.getConfig();
		Source db = bmeConfig.getDestination();
		
		try {
			if (stmt ==null){
				String strPreparedStatement = preparedStatement();
				logger.debug(strPreparedStatement);
				this.preparedStatement = db.getConnection().prepareStatement(strPreparedStatement);
			}else{
				this.preparedStatement = stmt;
			}
			IndexCounter index = new IndexCounter(1);
			for (CdmAttributeMapperBase mapper : this.mapperList){
				if (mapper instanceof IDbExportMapper){
					IDbExportMapper<DbExportState<?>> dbMapper = (IDbExportMapper)mapper;
					dbMapper.initialize(preparedStatement, index, state, berlinModelTableName);
				}else{
					logger.warn("mapper is not of type " + IDbExportMapper.class.getSimpleName());
				}
			}
			return true;
		} catch (SQLException e) {
			logger.warn("SQL Exception");
			throw e;
		}
	}
	
	public boolean invoke(CdmBase cdmBase) throws SQLException{
		try {
			boolean result = true;
			for (CdmAttributeMapperBase mapper : this.mapperList){
				if (mapper instanceof IDbExportMapper){
					IDbExportMapper<DbExportState<?>> dbMapper = (IDbExportMapper)mapper;
					try {
						result &= dbMapper.invoke(cdmBase);
					} catch (Exception e) {
						result = false;
						logger.error("Error occurred in mapping.invoke");
						e.printStackTrace();
						continue;
					}
				}else{
					logger.warn("mapper is not of type " + IDbExportMapper.class.getSimpleName());
				}
			}
			if (doExecute){
				int count = preparedStatement.executeUpdate();
				if (logger.isDebugEnabled())logger.debug("Number of rows affected: " + count);
			}
			return result;
		} catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage() + ": " + cdmBase.toString());
			return false;
		}
	}
	
	
	
	private String preparedStatement(){
		String sqlInsert = "INSERT INTO " + getBerlinModelTableName() + " (";
		String sqlValues = ") VALUES(";
		String sqlEnd = ")";
		String attributes = "";
		String values = "";
		for (String attribute : this.getDestinationAttributeList()){
			attributes +=  "," + attribute;
			values += ",?";
		}
		attributes = attributes.substring(1); //delete first ','
		values = values.substring(1); //delete first ','
		String result = sqlInsert + attributes + sqlValues + values + sqlEnd;
		return result;
	}

	/**
	 * @return the berlinModelTableName
	 */
	public String getBerlinModelTableName() {
		return berlinModelTableName;
	}

	/**
	 * @param berlinModelTableName the berlinModelTableName to set
	 */
	public void setBerlinModelTableName(String berlinModelTableName) {
		this.berlinModelTableName = berlinModelTableName;
	}

	/**
	 * @return the preparedStatement
	 */
	public PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}
	
	
	
}
