/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.CdmIoMapping;
import eu.etaxonomy.cdm.io.common.mapping.CdmMapperBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 */
public class CdmDbExportMapping<STATE extends DbExportStateBase<CONFIG, TRANSFORM>, CONFIG extends DbExportConfiguratorBase<STATE, TRANSFORM, Source>, TRANSFORM extends IExportTransformer>
        extends CdmIoMapping {

    private static final Logger logger = Logger.getLogger(CdmDbExportMapping.class);

	private PreparedStatement preparedStatement;
	private String dbTableName;
	private List<CollectionExportMapping<STATE,CONFIG, TRANSFORM>> collectionMappingList = new ArrayList<>();


	public CdmDbExportMapping(String tableName){
		this.dbTableName = tableName;
	}

	public boolean initialize(STATE state) throws SQLException{
		CONFIG config = state.getConfig();
		Source db = config.getDestination();

		try {
			IndexCounter index;
			String strPreparedStatement = prepareStatement();
			logger.debug(strPreparedStatement);
			this.preparedStatement = db.getConnection().prepareStatement(strPreparedStatement);
			index = new IndexCounter(1);

			for (CdmMapperBase mapper : this.mapperList){
				if (mapper instanceof IDbExportMapper){
					IDbExportMapper<DbExportStateBase<?,TRANSFORM>,TRANSFORM> dbMapper = (IDbExportMapper)mapper;
					dbMapper.initialize(preparedStatement, index, state, dbTableName);
				}else{
					logger.warn("mapper "+mapper.toString() + "," + mapper.getClass().getName() +" is not of type " + IDbExportMapper.class.getSimpleName());
				}
			}
			for (CollectionExportMapping<STATE,CONFIG, TRANSFORM> collectionMapping : this.collectionMappingList ){
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
			for (CdmMapperBase mapper : this.mapperList){
				if (mapper instanceof ObjectChangeMapper){
					ObjectChangeMapper changeMapper = (ObjectChangeMapper)mapper;
					cdmBase = changeMapper.getNewObject(cdmBase);
				}else if (mapper instanceof IDbExportMapper){
					IDbExportMapper<DbExportStateBase<?,TRANSFORM>, TRANSFORM> dbMapper = (IDbExportMapper)mapper;
					try {
						result &= dbMapper.invoke(cdmBase);
					} catch (Exception e) {
						result = false;
						logger.error("Error occurred in mapping.invoke");
						e.printStackTrace();
						continue;
					}
				}else {
					logger.warn("mapper " + mapper.toString() + "is not of required type " + IDbExportMapper.class.getSimpleName());
				}
			}
			int count = preparedStatement.executeUpdate();
			if (logger.isDebugEnabled()) {
                logger.debug("Number of rows affected: " + count);
            }
			for (CollectionExportMapping<STATE,CONFIG, TRANSFORM> collectionMapping : this.collectionMappingList ){
				result &= collectionMapping.invoke(cdmBase);
			}
			return result;
		} catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage() + ": " + cdmBase.toString());
			return false;
		}
	}


	public void addCollectionMapping(CollectionExportMapping<STATE,CONFIG, TRANSFORM> collectionMapping){
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

//	protected List<CdmAttributeMapperBase> getAttributeMapperList(){
//		List<CdmAttributeMapperBase> list = this.mapperList;
//		return list;
//	}


}
