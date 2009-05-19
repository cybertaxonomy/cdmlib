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
import java.util.ArrayList;
import java.util.List;

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
	private String dbTableName;
	private List<CollectionExportMapping> collectionMappingList = new ArrayList<CollectionExportMapping>();
	

	public BerlinModelExportMapping(String tableName){
		this.dbTableName = tableName;
	}
	
	public boolean initialize(BerlinModelExportState<?> state) throws SQLException{
		BerlinModelExportConfigurator bmeConfig = (BerlinModelExportConfigurator)state.getConfig();
		Source db = bmeConfig.getDestination();
		
		try {
			IndexCounter index;
			String strPreparedStatement = prepareStatement();
			logger.debug(strPreparedStatement);
			this.preparedStatement = db.getConnection().prepareStatement(strPreparedStatement);
			index = new IndexCounter(1);
			
			for (CdmAttributeMapperBase mapper : this.mapperList){
				if (mapper instanceof IDbExportMapper){
					IDbExportMapper<DbExportState<?>> dbMapper = (IDbExportMapper)mapper;
					dbMapper.initialize(preparedStatement, index, state, dbTableName);
				}else{
					logger.warn("mapper is not of type " + IDbExportMapper.class.getSimpleName());
				}
			}
			for (CollectionExportMapping collectionMapping : this.collectionMappingList ){
				collectionMapping.initialize(state);
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
			int count = preparedStatement.executeUpdate();
			if (logger.isDebugEnabled())logger.debug("Number of rows affected: " + count);
			for (CollectionExportMapping collectionMapping : this.collectionMappingList ){
				result &= collectionMapping.invoke(cdmBase);
			}
			return result;
		} catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage() + ": " + cdmBase.toString());
			return false;
		}
	}
	
	
	public void addCollectionMapping(CollectionExportMapping collectionMapping){
		this.collectionMappingList.add(collectionMapping);
	}
	
	protected String prepareStatement(){
		String sqlInsert = "INSERT INTO " + getDbTableName() + " (";
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
	public String getDbTableName() {
		return dbTableName;
	}

	/**
	 * @param berlinModelTableName the berlinModelTableName to set
	 */
	public void setDbTableName(String dbTableName) {
		this.dbTableName = dbTableName;
	}

	/**
	 * @return the preparedStatement
	 */
	protected PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}

	/**
	 * @param preparedStatement the preparedStatement to set
	 */
	protected void setPreparedStatement(PreparedStatement preparedStatement) {
		this.preparedStatement = preparedStatement;
	}
	
	
	
	
}
