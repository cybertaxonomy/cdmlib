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
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * This class retrives or creates an existing or a new feature.
 *
 * @see DbImportDefinedTermCreationMapperBase
 * @author a.mueller
 * @since 11.03.2010
 */
public class DbImportFeatureCreationMapper<STATE extends DbImportStateBase<?,?>> extends DbImportDefinedTermCreationMapperBase<Feature, DescriptionElementBase, DbImportStateBase<?,?>> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportFeatureCreationMapper.class);

//******************************** FACTORY METHOD ***************************************************/


	/**
	 *
	 * @param dbIdAttribute
	 * @param dbTermAttribute
	 * @param dbLabelAttribute
	 * @param dbLabelAbbrevAttribute
	 * @return
	 */
	public static DbImportFeatureCreationMapper<?> NewInstance(String dbIdAttribute, String featureNamespace, String dbTermAttribute, String dbLabelAttribute, String dbLabelAbbrevAttribute){
		return new DbImportFeatureCreationMapper(dbIdAttribute, featureNamespace, dbTermAttribute, dbLabelAttribute, dbLabelAbbrevAttribute);
	}


//	/**
//	 * Creates a Distribution with status <code>status</code> and adds it to the description of a taxon.
//	 * @param dbIdAttribute
//	 * @param objectToCreateNamespace
//	 * @param dbTaxonFkAttribute
//	 * @param taxonNamespace
//	 * @param status
//	 * @return
//	 */
//	public static DbImportFeatureCreationMapper<?> NewInstance(String dbIdAttribute, String dbTermAttribute, String dbLabelAttribute, String dbLabelAbbrevAttribute){
//		return new DbImportFeatureCreationMapper(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace, dbTextAttribute, language, feature, format);
//	}

//******************************* ATTRIBUTES ***************************************/


//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param dbIdAttribute
	 * @param objectToCreateNamespace
	 * @param dbTaxonFkAttribute
	 * @param taxonNamespace
	 */
	protected DbImportFeatureCreationMapper(String dbIdAttribute, String featureNamespace, String dbTermAttribute, String dbLabelAttribute, String dbLabelAbbrevAttribute) {
		super(dbIdAttribute, featureNamespace, dbTermAttribute, dbLabelAttribute, dbLabelAbbrevAttribute);
	}

//************************************ METHODS *******************************************/

	@Override
	protected Feature getTermFromState(UUID uuid) {
		return getState().getFeature(uuid);
	}

	@Override
	protected Feature getTermFromTransformer(String key, IInputTransformer transformer) throws UndefinedTransformerMethodException {
		return transformer.getFeatureByKey(key);
	}

	@Override
	protected UUID getUuidFromTransformer(String key, IInputTransformer transformer) throws UndefinedTransformerMethodException {
		UUID uuid = transformer.getFeatureUuid(key);
		return uuid;
	}

	@Override
	protected void saveTermToState(Feature feature) {
		getState().putFeature(feature);

	}

//	@Override
//	protected Feature createObject(ResultSet rs) throws SQLException {
//		String term = this.getStringDbValue(rs, dbTermAttribute);
//		String label = this.getStringDbValue(rs, dbLabelAttribute);
//		String labelAbbrev = this.getStringDbValue(rs, dbLabelAbbrevAttribute);
//		if (term != null || label != null || labelAbbrev != null){
//			Feature feature = Feature.NewInstance(term, label, labelAbbrev);
//			return feature;
//		}else{
//			return null;
//		}
//	}

	@Override
	protected Feature createDefinedTerm(ResultSet rs) throws SQLException {
		String term = this.getStringDbValue(rs, dbTermAttribute);
		String label = this.getStringDbValue(rs, dbLabelAttribute);
		String labelAbbrev = this.getStringDbValue(rs, dbLabelAbbrevAttribute);
		if (term != null || label != null || labelAbbrev != null){
			Feature feature = Feature.NewInstance(term, label, labelAbbrev);
			return feature;
		}else{
			return null;
		}
	}

}
