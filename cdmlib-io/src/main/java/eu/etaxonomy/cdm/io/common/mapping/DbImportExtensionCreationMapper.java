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
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;


/**
 *  Object creation mapper which creates a extensions.
 *  
 * @author a.mueller
 * @since 11.03.2010
 * @version 1.0
 */
public class DbImportExtensionCreationMapper extends DbImportSupplementCreationMapperBase<Extension, IdentifiableEntity, DbImportStateBase<?, ?>, ExtensionType > {
	private static final Logger logger = Logger.getLogger(DbImportExtensionCreationMapper.class);

//************************** FACTORY METHODS ***************************************************************/
	
	
	/**
	 * Creates an extension mapper which creates an empty extension.
	 *
	 * @param dbExtendedObjectAttribute the database column that holds the foreign key for the extended object - obligatory
	 * @param extendedObjectNamespace the namespace under which the extended object has been saved previously - obligatory
	 * @return DbImportExtensionCreationMapper
	 */
	public static DbImportExtensionCreationMapper NewInstance(String dbExtendedObjectAttribute, String extendedObjectNamespace){
		return new DbImportExtensionCreationMapper(dbExtendedObjectAttribute, extendedObjectNamespace, null,  null, null);
	}
	
	/**
	 * Creates an extension mapper which creates an extension and sets the extension value,
	 * the extension type and adds an annotation holding the original source id
	 *
	 * @param dbExtendedObjectAttribute the database column that holds the foreign key for the extended object - obligatory
	 * @param extendedObjectNamespace the namespace under which the extended object has been saved previously - obligatory
	 * @param dbExtensionValueAttribute if null no extension value is set
	 * @param dbIdAttribute if null not original source id annotation is added
	 * @param extensionType if null no extension type is set
	 * @return DbImportExtensionCreationMapper
	 */
	public static DbImportExtensionCreationMapper NewInstance( String dbExtendedObjectAttribute, String extendedObjectNamespace, String dbExtensionValueAttribute,String dbIdAttribute, ExtensionType extensionType){
		return new DbImportExtensionCreationMapper(dbExtendedObjectAttribute, extendedObjectNamespace, dbExtensionValueAttribute, dbIdAttribute, extensionType);
	}

	
//********************************* CONSTRUCTOR ****************************************/

	protected DbImportExtensionCreationMapper(String dbSupplementedObjectAttribute, String supplementedObjectNamespace, String dbSupplementValueAttribute, String dbIdAttribute, ExtensionType supplementType) {
		super(dbSupplementValueAttribute, dbSupplementedObjectAttribute, dbIdAttribute, supplementedObjectNamespace, supplementType);
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportSupplementCreationMapperBase#addSupplement(eu.etaxonomy.cdm.model.common.AnnotatableEntity, java.lang.String, eu.etaxonomy.cdm.model.common.AnnotatableEntity)
	 */
	@Override
	protected boolean addSupplement(Extension extension, IdentifiableEntity identifiableEntity, String id) {
		if (identifiableEntity != null){
			identifiableEntity.addExtension(extension);
			return true;
		}else{
			String warning = "Identifiable entity (" + id + ") for extension not found. Extension not created.";
			logger.warn(warning);
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportSupplementCreationMapperBase#setSupplementValue(java.lang.Object)
	 */
	@Override
	protected void setSupplementValue(ResultSet rs, Extension extension) throws SQLException {
		String value = rs.getString(dbSupplementValueAttribute);
		extension.setValue(value);
		
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#createObject(java.sql.ResultSet)
	 */
	@Override
	protected Extension createObject(ResultSet rs) throws SQLException {
		Extension extension = Extension.NewInstance();
		extension.setType(this.supplementType);
		return extension;
	}

	
}
