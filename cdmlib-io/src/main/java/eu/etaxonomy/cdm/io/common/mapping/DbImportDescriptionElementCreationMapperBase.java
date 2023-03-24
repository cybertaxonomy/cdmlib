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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @since 11.03.2010
 */
public abstract class DbImportDescriptionElementCreationMapperBase<ELEMENT extends DescriptionElementBase, STATE extends DbImportStateBase<?,?>> extends DbImportObjectCreationMapperBase<ELEMENT, STATE> {

    private static final Logger logger = LogManager.getLogger();

//******************************* ATTRIBUTES ***************************************/

	protected String taxonNamespace;
	protected String dbTaxonFkAttribute;
	protected boolean isImageGallery = false;
	protected String dbCitationAttribute;  //if there is a single source available
	protected String sourceNamespace;
	protected String dbMicroCitationAttribute;

//********************************* CONSTRUCTOR ****************************************/

	protected DbImportDescriptionElementCreationMapperBase(String dbIdAttribute, String objectToCreateNamespace, String dbTaxonFkAttribute, String taxonNamespace) {
		super(dbIdAttribute, objectToCreateNamespace);
		this.taxonNamespace = taxonNamespace;
		this.dbTaxonFkAttribute = dbTaxonFkAttribute;
	}

//************************************ METHODS *******************************************/

	public DbImportDescriptionElementCreationMapperBase<ELEMENT,STATE> setSource(String dbCitationAttribute, String sourceNamespace, String dbMicroCitationAttribute){
		this.dbCitationAttribute = dbCitationAttribute;
		this.sourceNamespace = sourceNamespace;
		this.dbMicroCitationAttribute = dbMicroCitationAttribute;
		return this;
	}

	@Override
	protected ELEMENT doInvoke(ResultSet rs, ELEMENT element) throws SQLException {
		Taxon taxon = getAcceptedTaxon(rs);
		if (taxon != null){
			element = addDescriptionElement(taxon, element);
		}else{
			logger.info("Taxon could not be determined. Description element was not add to any description or taxon");
		}
		//Source
		if (StringUtils.isNotBlank(dbCitationAttribute)){
			addSource(rs, element);
		}
		return element;
	}

	private void addSource(ResultSet rs, ELEMENT element) throws SQLException {
		String microCitation = getStringDbValue(rs, dbMicroCitationAttribute);
		Reference citation = (Reference) getState().getRelatedObject(sourceNamespace, String.valueOf(rs.getObject(dbCitationAttribute)));
		element.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, citation, microCitation);
	}

	protected Taxon getAcceptedTaxon(ResultSet rs) throws SQLException {
		String taxonFk = getForeignKey(rs, dbTaxonFkAttribute);
		TaxonBase<?> taxonBase = (TaxonBase<?>)getRelatedObject(taxonNamespace, taxonFk);
		Taxon taxon = null;
		if (taxonBase == null){
			logger.warn("TaxonBase not found: " + taxonFk);
		}else if (taxonBase instanceof Taxon){
			taxon = (Taxon)taxonBase;

		}else if (taxonBase instanceof Synonym){
			Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
			Taxon accTaxon = synonym.getAcceptedTaxon();
			if (accTaxon == null){
				logger.warn("Synonym '" + synonym.getTitleCache() + "' ("+ taxonFk + ") has no accepted taxon. Can't define a taxon to add the description element to");
			}else {
				taxon = accTaxon;
			}
		}else{ //null
			throw new IllegalStateException("TaxonBase must either be null, Taxon or Synonym but was something else");
		}
		return taxon;
	}

	/**
	 * Adds a description element to the taxon's first description which is not an image gallery.
	 * If no such description exists a new one is generated. Returns the element or, if null if taxon is null.
	 */
	protected ELEMENT addDescriptionElement(Taxon taxon, ELEMENT element) {
		if (taxon == null){
			return null;
		}else{
			TaxonDescription description = getTaxonDescription(taxon, isImageGallery);
			description.addElement(element);
			return element;
		}
	}

	protected TaxonDescription getTaxonDescription(Taxon taxon, boolean isImageGallery) {
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		TaxonDescription description = null;
		if (descriptions.size() > 0){
			for (TaxonDescription desc : descriptions){
				if ( desc.isImageGallery() == isImageGallery ){
					description = desc;
					break;
				}
			}
		}
		if (description == null){
			description = TaxonDescription.NewInstance(taxon, isImageGallery);
		}
		return description;
	}
}
