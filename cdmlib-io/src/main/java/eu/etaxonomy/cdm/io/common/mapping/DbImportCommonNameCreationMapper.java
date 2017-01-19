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
import eu.etaxonomy.cdm.model.description.CommonTaxonName;

/**
 * @author a.mueller
 * @created 11.03.2010
 * @version 1.0
 */
public class DbImportCommonNameCreationMapper<STATE extends DbImportStateBase<?,?>> extends DbImportDescriptionElementCreationMapperBase<CommonTaxonName, DbImportStateBase<?,?>> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportCommonNameCreationMapper.class);

//******************************** FACTORY METHOD ***************************************************/
	
	public static DbImportCommonNameCreationMapper<?> NewInstance(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace){
		return new DbImportCommonNameCreationMapper(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace);
	}
	
//******************************* ATTRIBUTES ***************************************/

	
//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param dbIdAttribute
	 * @param objectToCreateNamespace
	 * @param dbTaxonFkAttribute
	 * @param taxonNamespace
	 */
	protected DbImportCommonNameCreationMapper(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace) {
		super(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace);
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#createObject(java.sql.ResultSet)
	 */
	@Override
	protected CommonTaxonName createObject(ResultSet rs) throws SQLException {
		CommonTaxonName commonName = CommonTaxonName.NewInstance(null, null);
		return commonName;
	}

}
