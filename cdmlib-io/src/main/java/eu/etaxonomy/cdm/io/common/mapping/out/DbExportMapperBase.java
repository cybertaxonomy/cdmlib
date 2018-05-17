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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public class DbExportMapperBase<STATE extends DbExportStateBase<?, IExportTransformer>>  /*implements IDbExportMapper */{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbExportMapperBase.class);

	protected PreparedStatement preparedStatement = null;
	protected int index = 0;
	private STATE state;
	private String tableName;
	



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper#initialize(java.sql.PreparedStatement, int)
	 */
	public void initialize(PreparedStatement stmt, IndexCounter index, STATE state, String tableName) {
		this.preparedStatement = stmt;
		this.index = index.getIncreasing();
		this.state = state;
		this.tableName = tableName;
	}
	

	public void initializeNull(PreparedStatement stmt, STATE state, String tableName) {
		this.preparedStatement = stmt;
		this.index = -1;
		this.state = state;
		this.tableName = tableName;
	}


	/**
	 * @return the preparedStatement
	 */
	public PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}
	
	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}


	/**
	 * @return the state
	 */
	public STATE getState() {
		return state;
	}


	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}


	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
