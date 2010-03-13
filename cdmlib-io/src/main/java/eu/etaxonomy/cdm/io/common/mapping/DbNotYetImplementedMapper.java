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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This mapper does not change any import object, but keeps the information that a database
 * attribute needs to be mapped but is not yet mapped.
 * @author a.mueller
 * @created 25.02.2010
 * @version 1.0
 */
public class DbNotYetImplementedMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase, CdmBase> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbNotYetImplementedMapper.class);
	
	public static DbNotYetImplementedMapper NewInstance(String dbAttributeToIgnore){
		return new DbNotYetImplementedMapper(dbAttributeToIgnore, null, null);
	}
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 * @param defaultValue
	 */
	protected DbNotYetImplementedMapper(String dbAttributString, String cdmAttributeString, Object defaultValue) {
		super(dbAttributString, cdmAttributeString, defaultValue);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	public CdmBase invoke(ResultSet rs, CdmBase cdmBase) throws SQLException {
		return cdmBase; //do nothing
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class getTypeClass() {
		return null;  //not needed
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
	@Override
	public void initialize(DbImportStateBase state, Class<? extends CdmBase> destinationClass) {
		String attributeName = this.getSourceAttribute();
		logger.warn(attributeName + " not yet implemented for class " + destinationClass.getSimpleName() );
	}

	
	
	
}
