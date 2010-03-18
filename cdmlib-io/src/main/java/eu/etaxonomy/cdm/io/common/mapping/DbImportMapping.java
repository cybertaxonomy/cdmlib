// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IndexCounter;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbImportMapping<STATE extends DbImportStateBase, CONFIG extends IImportConfigurator> extends CdmIoMapping {
	private static final Logger logger = Logger.getLogger(DbImportMapping.class);
	
//	private PreparedStatement preparedStatement;
	private String dbTableName;
	//private List<CollectionExportMapping> collectionMappingList = new ArrayList<CollectionExportMapping>();

	public DbImportMapping(String tableName){
		this.dbTableName = tableName;
	}
	
	public boolean initialize(){
		return true;
	}
	
//	public boolean initialize(STATE state) throws SQLException{
//		CONFIG config = (CONFIG)state.getConfig();
//		ICdmDataSource db = config.getDestination();
//		
//		try {
//			IndexCounter index;
//			String strPreparedStatement = prepareStatement();
//			logger.debug(strPreparedStatement);
//			this.preparedStatement = db.getConnection().prepareStatement(strPreparedStatement);
//			index = new IndexCounter(1);
//			
//			for (CdmAttributeMapperBase mapper : this.mapperList){
//				if (mapper instanceof IDbExportMapper){
//					IDbExportMapper<DbExportStateBase<?>> dbMapper = (IDbExportMapper)mapper;
//					dbMapper.initialize(preparedStatement, index, state, dbTableName);
//				}else{
//					logger.warn("mapper is not of type " + IDbExportMapper.class.getSimpleName());
//				}
//			}
//			for (CollectionExportMapping collectionMapping : this.collectionMappingList ){
//				collectionMapping.initialize(state);
//			}
//			return true;
//		} catch (SQLException e) {
//			logger.warn("SQL Exception");
//			throw e;
//		}
//	}

	
	public boolean invoke(ResultSet rs, Set<CdmBase> objectsToSave) throws SQLException{
		boolean result = true;
		CdmBase objectToSave = null;
//		try {
			for (CdmAttributeMapperBase mapper : this.mapperList){
				if (mapper instanceof IDbImportMapper){
					IDbImportMapper<DbImportStateBase<?>,CdmBase> dbMapper = (IDbImportMapper)mapper;
					try {
						objectToSave = dbMapper.invoke(rs, objectToSave);
					} catch (Exception e) {
						result = false;
						logger.error("Error occurred in mapping.invoke");
						e.printStackTrace();
						continue;
					}
				}else{
					logger.warn("mapper is not of type " + IDbImportMapper.class.getSimpleName());
				}
			}
			objectsToSave.add(objectToSave);
//			int count = preparedStatement.executeUpdate();
//			if (logger.isDebugEnabled())logger.debug("Number of rows affected: " + count);
//			for (CollectionExportMapping collectionMapping : this.collectionMappingList ){
//				result &= collectionMapping.invoke(cdmBase);
//			}
			return result;
//		} catch(SQLException e){
//			e.printStackTrace();
//			logger.error(e.getMessage() + ": " + rs.toString());
//			return null;
//		}
	}
	
	
//	public void addCollectionMapping(CollectionExportMapping collectionMapping){
//		this.collectionMappingList.add(collectionMapping);
//	}
	
//	protected String prepareStatement(){
//		String sqlInsert = "INSERT INTO " + getDbTableName() + " (";
//		String sqlValues = ") VALUES(";
//		String sqlEnd = ")";
//		String attributes = "";
//		String values = "";
//		for (String attribute : this.getDestinationAttributeList()){
//			attributes +=  "," + attribute;
//			values += ",?";
//		}
//		attributes = attributes.substring(1); //delete first ','
//		values = values.substring(1); //delete first ','
//		String result = sqlInsert + attributes + sqlValues + values + sqlEnd;
//		return result;
//	}

//	/**
//	 * @return the berlinModelTableName
//	 */
//	public String getDbTableName() {
//		return dbTableName;
//	}
//
//	/**
//	 * @param berlinModelTableName the berlinModelTableName to set
//	 */
//	public void setDbTableName(String dbTableName) {
//		this.dbTableName = dbTableName;
//	}
//
//	/**
//	 * @return the preparedStatement
//	 */
//	protected PreparedStatement getPreparedStatement() {
//		return preparedStatement;
//	}
//
//	/**
//	 * @param preparedStatement the preparedStatement to set
//	 */
//	protected void setPreparedStatement(PreparedStatement preparedStatement) {
//		this.preparedStatement = preparedStatement;
//	}
//	
//	protected List<CdmAttributeMapperBase> getAttributeMapperList(){
//		List<CdmAttributeMapperBase> list = this.mapperList;
//		return list;
//	}
	
	
}
