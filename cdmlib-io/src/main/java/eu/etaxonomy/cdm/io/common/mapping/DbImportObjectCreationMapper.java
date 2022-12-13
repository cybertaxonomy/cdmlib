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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbImportObjectCreationMapper<ANNOTATABLE extends AnnotatableEntity, STATE extends DbImportStateBase<?,?>>
        extends DbImportObjectCreationMapperBase<ANNOTATABLE, STATE> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

//******************************** FACTORY METHOD ***************************************************/

	public static DbImportObjectCreationMapper<?,?> NewInstance(IMappingImport mappingImport, String dbIdAttribute, String namespace){
		return new DbImportObjectCreationMapper<>(mappingImport, dbIdAttribute, namespace);
	}

//******************************* ATTRIBUTES ***************************************/
	private IMappingImport<ANNOTATABLE, STATE> mappingImport;


//********************************* CONSTRUCTOR ****************************************/

	protected DbImportObjectCreationMapper(IMappingImport<ANNOTATABLE, STATE> mappingImport, String dbIdAttribute, String objectToCreateNamespace) {
		super(dbIdAttribute, objectToCreateNamespace);
		this.mappingImport = mappingImport;
	}

//************************************ METHODS *******************************************/

	@Override
	protected ANNOTATABLE doInvoke(ResultSet rs, ANNOTATABLE createdObject) throws SQLException {
		// do nothing
		return createdObject;
	}

	@Override
	protected ANNOTATABLE createObject(ResultSet rs) throws SQLException {
		ANNOTATABLE result = mappingImport.createObject(rs, importMapperHelper.getState());
		return result;
	}
}