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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbExportMapperBase<STATE extends DbExportStateBase<?, IExportTransformer>>  /*implements IDbExportMapper */{

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	protected PreparedStatement preparedStatement = null;
	protected int index = 0;
	private STATE state;
	private String tableName;

	public void initialize(PreparedStatement stmt, IndexCounter index, STATE state, String tableName) {
		this.preparedStatement = stmt;
		this.index = index.getIncreasing();
		this.state = state;
		this.tableName = tableName;
	}

	/**
	 * Initialize the mapper in a way that it does not add any data
	 * to the prepared statement.
	 */
	public void initializeNull(PreparedStatement stmt, STATE state, String tableName) {
		this.preparedStatement = stmt;
		this.index = -1;
		this.state = state;
		this.tableName = tableName;
	}

	public PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}

	public int getIndex() {
		return index;
	}

	public STATE getState() {
		return state;
	}

	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
