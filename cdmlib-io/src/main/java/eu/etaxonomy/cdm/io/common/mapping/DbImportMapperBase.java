/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbImportMapperBase<STATE extends DbImportStateBase<?,?>>  {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private STATE state;
	private String tableName;
	private Class<? extends CdmBase> cdmClass;

	public void initialize(STATE state, Class<? extends CdmBase> cdmClass) {
		this.state = state;
		this.cdmClass = cdmClass;
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

    public Class<? extends CdmBase> getCdmClass() {
        return cdmClass;
    }
	public void setCdmClass(Class<? extends CdmBase> cdmClass) {
		this.cdmClass = cdmClass;
	}
}
