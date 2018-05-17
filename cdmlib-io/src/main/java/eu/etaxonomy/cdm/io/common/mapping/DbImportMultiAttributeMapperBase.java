/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.03.2010
 * @version 1.0
 */
public abstract class DbImportMultiAttributeMapperBase<CDMBASE extends CdmBase, STATE extends DbImportStateBase<?,?>> extends MultipleAttributeMapperBase<CdmSingleAttributeMapperBase> implements IDbImportMapper<STATE, CDMBASE>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportMultiAttributeMapperBase.class);
	
//**************************** ATTRIBUTES ****************************************************
	
	protected DbImportMapperBase<STATE> importMapperHelper = new DbImportMapperBase<STATE>();
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
	public void initialize(STATE state, Class<? extends CdmBase> destinationClass) {
		importMapperHelper.initialize(state, destinationClass);
	} 
	
	protected STATE getState(){
		return importMapperHelper.getState();
	}
	

	/**
	 * Retrieves a related object from the state's related object map. Needs casting.
	 * @param namespace
	 * @param foreignKey
	 * @return
	 */
	protected CdmBase getRelatedObject(String namespace, String foreignKey) {
		STATE state = importMapperHelper.getState();
		CdmBase result = state.getRelatedObject(namespace, foreignKey);
		return result;
	}
	
	/**
	 * Retrieves a related object from the state's related object map. Needs casting.
	 * @param namespace
	 * @param foreignKey
	 * @return
	 * @throws SQLException 
	 */
	protected CdmBase getRelatedObject(ResultSet rs, String namespace, String fkAttribute) throws SQLException {
		STATE state = importMapperHelper.getState();
		String foreignKey = getForeignKey(rs, fkAttribute);
		CdmBase result = state.getRelatedObject(namespace, foreignKey);
		return result;
	}
	
	/**
	 * @param rs
	 * @param dbReferenceFkAttribute2
	 * @return
	 * @throws SQLException 
	 */
	protected String getForeignKey(ResultSet rs, String fkAttribute) throws SQLException {
		Object oForeignKey = rs.getObject(fkAttribute);
		String result = String.valueOf(oForeignKey);
		return result;
	}
	
	
}
