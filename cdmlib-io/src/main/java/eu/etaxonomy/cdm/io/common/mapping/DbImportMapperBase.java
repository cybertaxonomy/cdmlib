/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

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
public class DbImportMapperBase<STATE extends DbImportStateBase<?,?>>  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportMapperBase.class);

	private STATE state;
	private String tableName;
	private Class<? extends CdmBase> cdmClass;


	/**
	 * @param state
	 * @param tableName
	 */
	public void initialize(STATE state, Class<? extends CdmBase> cdmClass) {
		this.state = state;
		this.cdmClass = cdmClass;
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

	/**
	 * @param cdmClass the cdmClass to set
	 */
	public void setCdmClass(Class<? extends CdmBase> cdmClass) {
		this.cdmClass = cdmClass;
	}

	/**
	 * @return the cdmClass
	 */
	public Class<? extends CdmBase> getCdmClass() {
		return cdmClass;
	}
}
