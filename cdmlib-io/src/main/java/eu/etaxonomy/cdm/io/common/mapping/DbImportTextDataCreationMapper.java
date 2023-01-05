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

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;

/**
 * This Mapper creates a text data element and adds it to a taxon.
 *
 * @author a.mueller
 * @since 11.03.2010
 */
public class DbImportTextDataCreationMapper<STATE extends DbImportStateBase<?,?>>
        extends DbImportDescriptionElementCreationMapperBase<TextData, DbImportStateBase<?,?>> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

//******************************** FACTORY METHOD ***************************************************/

	/**
	 * Creates a TextData and adds it to the description of a taxon.
	 */
	public static DbImportTextDataCreationMapper<?> NewInstance(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace){
		Feature feature = null;
		Language language = null;
		TextFormat format = null;
		String dbTextAttribute = null;
		return new DbImportTextDataCreationMapper<>(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace, dbTextAttribute, language, feature, format);
	}

	/**
	 * Creates a TextData, adds the the in the language and the format defined and then adds it to the description of a taxon.
	 * If language is <code>null</code> the default language is taken instead.
	 */
	public static DbImportTextDataCreationMapper<?> NewInstance(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace, String dbTextAttribute, Language language, Feature feature, TextFormat format){
		return new DbImportTextDataCreationMapper<>(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace, dbTextAttribute, language, feature, format);
	}

//******************************* ATTRIBUTES ***************************************/

	protected Feature defaultFeature;
	protected Language defaultLanguage;
	protected TextFormat defaultFormat;
	protected String dbTextAttribute;

//********************************* CONSTRUCTOR ****************************************/

	protected DbImportTextDataCreationMapper(String dbIdAttribute, String objectToCreateNamespace,
	        String dbTaxonFkAttribute, String taxonNamespace, String dbTextAttribute, Language language,
	        Feature feature, TextFormat format) {
		super(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace);
		this.defaultFeature = feature;
		this.dbTextAttribute = dbTextAttribute;
		this.defaultLanguage = language;
		this.defaultFormat = format;
	}

//************************************ METHODS *******************************************/

	@Override
	protected TextData createObject(ResultSet rs) throws SQLException {
		TextData textData = TextData.NewInstance();
		String text = null;
		if (StringUtils.isNotBlank(dbTextAttribute)){
			text = rs.getString(dbTextAttribute);
		}
		if (text != null){
			Language language = this.defaultLanguage;
			if (language == null){
				language = Language.DEFAULT();
			}
			textData.putText(language, text);
		}
		TextFormat format = this.defaultFormat;
		textData.setFormat(format);

		Feature feature = this.defaultFeature;
		textData.setFeature(feature);
		return textData;
	}
}