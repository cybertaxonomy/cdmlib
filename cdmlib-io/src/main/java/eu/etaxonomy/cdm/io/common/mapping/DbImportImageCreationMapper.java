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
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @since 11.03.2010
 */
public class DbImportImageCreationMapper extends DbImportDescriptionElementCreationMapperBase<TextData, DbImportStateBase<?,?>> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

// ************************** FACTORY ***********************************************************/

	public static DbImportImageCreationMapper NewInstance(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace, boolean isOneTextData){
		return new DbImportImageCreationMapper(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace, isOneTextData);
	}

// ************************** ATTRIBUTES ***********************************************************/

	private boolean isOneTextData;

//********************************* CONSTRUCTOR ***************************************************/

	protected DbImportImageCreationMapper(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace, boolean isOneTextData) {
		super(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace);
		this.isImageGallery = true;
		this.isOneTextData = isOneTextData;
	}

// ********************************** METHODS ***********************************************************

	@Override
	protected TextData createObject(ResultSet rs) throws SQLException {
		TextData textData = TextData.NewInstance(Feature.IMAGE());
		return textData;
	}

	@Override
	protected TextData addDescriptionElement(Taxon taxon, TextData element) {
		if (taxon == null){
			return null;
		}else{
			TaxonDescription description = getTaxonDescription(taxon, isImageGallery);
			if (isOneTextData == true){

				Set<DescriptionElementBase> elements = description.getElements();
				for (DescriptionElementBase descElement : elements){
					if (descElement.isInstanceOf(TextData.class) && descElement.getFeature().equals(Feature.IMAGE()) ){
						element = CdmBase.deproxy(descElement, TextData.class);
					}
				}
			}
			description.addElement(element);
			return element;
		}
	}
}