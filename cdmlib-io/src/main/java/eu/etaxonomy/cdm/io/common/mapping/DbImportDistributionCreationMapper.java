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
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;

/**
 * This Mapper creates a distribution and adds it to a taxon.
 * @author a.mueller
 * @created 11.03.2010
 * @version 1.0
 */
public class DbImportDistributionCreationMapper<STATE extends DbImportStateBase<?,?>> extends DbImportDescriptionElementCreationMapperBase<Distribution, DbImportStateBase<?,?>> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportDistributionCreationMapper.class);

//******************************** FACTORY METHOD ***************************************************/

	
	/**
	 * Creates a Distribution and adds it to the description of a taxon. 
	 * @param dbIdAttribute
	 * @param objectToCreateNamespace
	 * @param dbTaxonFkAttribute
	 * @param taxonNamespace
	 * @return
	 */
	public static DbImportDistributionCreationMapper<?> NewInstance(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace){
		PresenceAbsenceTerm status = null;
		return new DbImportDistributionCreationMapper(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace, status);
	}
	
	
	/**
	 * Creates a Distribution with status <code>status</code> and adds it to the description of a taxon. 
	 * @param dbIdAttribute
	 * @param objectToCreateNamespace
	 * @param dbTaxonFkAttribute
	 * @param taxonNamespace
	 * @param status
	 * @return
	 */
	public static DbImportDistributionCreationMapper<?> NewFixedStatusInstance(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace, PresenceAbsenceTerm status){
		return new DbImportDistributionCreationMapper(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace, status);
	}
	
//******************************* ATTRIBUTES ***************************************/

	protected PresenceAbsenceTerm status;
	
//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param dbIdAttribute
	 * @param objectToCreateNamespace
	 * @param dbTaxonFkAttribute
	 * @param taxonNamespace
	 */
	protected DbImportDistributionCreationMapper(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace, PresenceAbsenceTerm status) {
		super(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace);
	}

//************************************ METHODS *******************************************/

	@Override
	protected Distribution createObject(ResultSet rs) throws SQLException {
		Distribution distribution = Distribution.NewInstance(null, null);
		distribution.setStatus(status);
		return distribution;
	}

}
