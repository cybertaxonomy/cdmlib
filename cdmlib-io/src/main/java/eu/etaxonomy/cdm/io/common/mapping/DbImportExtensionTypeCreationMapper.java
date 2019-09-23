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
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * This class retrieves or creates an existing or a new extension type.
 * Does NOT create and add an extension to the passed object as further information
 * is needed to create an extension.
 *
 * @see DbImportDefinedTermCreationMapperBase
 * @author a.mueller
 * @since 11.03.2010
 */
public class DbImportExtensionTypeCreationMapper<STATE extends DbImportStateBase<?,?>>
        extends DbImportDefinedTermCreationMapperBase<ExtensionType, IdentifiableEntity, DbImportStateBase<?,?>> {

	private static final Logger logger = Logger.getLogger(DbImportExtensionTypeCreationMapper.class);

//******************************** FACTORY METHOD ***************************************************/

	/**
	 *
	 * @param dbIdAttribute
	 * @parem extensionTypeNamespace
	 * @param dbTermAttribute
	 * @param dbLabelAttribute
	 * @param dbLabelAbbrevAttribute
	 * @return
	 */
	public static DbImportExtensionTypeCreationMapper<?> NewInstance(String dbIdAttribute, String extensionTypeNamespace, String dbTermAttribute, String dbLabelAttribute, String dbLabelAbbrevAttribute){
		return new DbImportExtensionTypeCreationMapper<>(dbIdAttribute, extensionTypeNamespace, dbTermAttribute, dbLabelAttribute, dbLabelAbbrevAttribute);
	}

//********************************* CONSTRUCTOR ****************************************/

	/**
	 * @param dbIdAttribute
	 * @param extensionTypeNamespace
	 * @param dbTermAttribute
	 * @param dbLabelAttribute
	 * @param dbLabelAbbrevAttribute
	 */
	protected DbImportExtensionTypeCreationMapper(String dbIdAttribute, String extensionTypeNamespace,
	        String dbTermAttribute, String dbLabelAttribute, String dbLabelAbbrevAttribute) {
		super(dbIdAttribute, extensionTypeNamespace, dbTermAttribute, dbLabelAttribute, dbLabelAbbrevAttribute);
	}

//************************************ METHODS *******************************************/

	@Override
	protected ExtensionType getTermFromState(UUID uuid) {
		return getState().getExtensionType(uuid);
	}

	@Override
	protected ExtensionType getTermFromTransformer(String key, IInputTransformer transformer) throws UndefinedTransformerMethodException {
		return transformer.getExtensionTypeByKey(key);
	}

	@Override
	protected UUID getUuidFromTransformer(String key, IInputTransformer transformer) throws UndefinedTransformerMethodException {
		UUID uuid = transformer.getExtensionTypeUuid(key);
		return uuid;
	}

	@Override
	protected void saveTermToState(ExtensionType extensionType) {
		getState().putExtensionType(extensionType);
	}

	@Override
	protected ExtensionType createDefinedTerm(ResultSet rs) throws SQLException {
		String term = this.getStringDbValue(rs, dbTermAttribute);
		String label = this.getStringDbValue(rs, dbLabelAttribute);
		String labelAbbrev = this.getStringDbValue(rs, dbLabelAbbrevAttribute);
		if (term != null || label != null || labelAbbrev != null){
			ExtensionType definedTerm = ExtensionType.NewInstance(term, label, labelAbbrev);
			return definedTerm;
		}else{
			return null;
		}
	}

    @Override
    protected void handleTermWithObject(IdentifiableEntity entity, ExtensionType extensionType) {
        //Not yet implemented. Needs further information on extension, which is not available here
    }

}
