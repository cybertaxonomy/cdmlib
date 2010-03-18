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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbImportObjectCreationMapper<ANNOTATABLE extends AnnotatableEntity, STATE extends DbImportStateBase<?,?>> extends DbImportObjectCreationMapperBase<ANNOTATABLE, STATE> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportObjectCreationMapper.class);
	
	public static DbImportObjectCreationMapper<?> NewInstance(IMappingImport mappingImport){
		return new DbImportObjectCreationMapper(mappingImport);
	}
	
//******************************* ATTRIBUTES ***************************************/
	private IMappingImport<ANNOTATABLE, STATE> mappingImport;
		
	

	
	/**
	 * @param parameterTypes 
	 * @param dbIdAttributString
	 */
	protected DbImportObjectCreationMapper(IMappingImport<ANNOTATABLE, STATE> mappingImport, String dbIdAttribute, String objectToCreateNamespace) {
		super(dbIdAttribute, objectToCreateNamespace);
		this.mappingImport = mappingImport;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#doInvoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.VersionableEntity)
	 */
	@Override
	protected ANNOTATABLE doInvoke(ResultSet rs, ANNOTATABLE createdObject) throws SQLException {
		// do nothing
		return createdObject;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#createObject(java.sql.ResultSet)
	 */
	@Override
	protected ANNOTATABLE createObject(ResultSet rs) throws SQLException {
		ANNOTATABLE result = mappingImport.createObject(rs, importMapperHelper.getState());
		return result;
	}

}
