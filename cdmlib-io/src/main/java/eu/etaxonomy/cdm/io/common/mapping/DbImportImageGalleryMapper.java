/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.update.DatabaseTypeNotSupportedException;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * This class maps a database attribute to CDM extension added to the target class
 * TODO maybe this class should not inherit from DbSingleAttributeImportMapperBase
 * as it does not map to a single attribute
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public class DbImportImageGalleryMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, Taxon> implements IDbImportMapper<DbImportStateBase<?,?>,Taxon>{
	private static final Logger logger = Logger.getLogger(DbImportImageGalleryMapper.class);

//************************** FACTORY METHODS ***************************************************************/

	/**
	 * @param dbAttributeString
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	public static DbImportImageGalleryMapper NewInstance(String dbAttributeString){
		return new DbImportImageGalleryMapper(dbAttributeString);
	}

//***************** VARIABLES **********************************************************/


//******************************** CONSTRUCTOR *****************************************************************/

	/**
	 * @param dbAttributeString
	 * @param extensionType
	 */
	private DbImportImageGalleryMapper(String dbAttributeString) {
		super(dbAttributeString, dbAttributeString);
	}

//****************************** METHODS ***************************************************/

	/**
	 * @param service
	 * @param state
	 * @param tableName
	 */
	public void initialize(ITermService service, DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass) {
		importMapperHelper.initialize(state, destinationClass);
		try {
			if (  checkDbColumnExists()){
				//do nothing
			}else{
				ignore = true;
			}
		} catch (DatabaseTypeNotSupportedException e) {
			//do nothing
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public TaxonBase invoke(ResultSet rs, TaxonBase taxonBase) throws SQLException {
		String dbValue = rs.getString(getSourceAttribute());
		return invoke(dbValue, taxonBase);
	}

	/**
	 * @param dbValue
	 * @param identifiableEntity
	 * @return
	 */
	private TaxonBase invoke(String dbValue, TaxonBase taxonBase){
		if (ignore || CdmUtils.isEmpty(dbValue)){
			return taxonBase;
		}
		boolean createNew = true;
		Taxon taxon;
		if (taxonBase.isInstanceOf(Synonym.class)){
			Synonym synonym = taxonBase.deproxy(taxonBase, Synonym.class);
			if (synonym.getAcceptedTaxon() != null){
				logger.warn("Media will be added to a synonyms accepted taxon");
				taxon = synonym.getAcceptedTaxon();
			}else{
				throw new IllegalArgumentException("TaxonBase was of type synonym and does not belong to an accepted taxon");
			}
		}else{
			taxon = taxonBase.deproxy(taxonBase, Taxon.class);
		}

		TaxonDescription imageGallery = taxon.getImageGallery(createNew);
		Set<DescriptionElementBase> elements = imageGallery.getElements();
		DescriptionElementBase element = null;
		if (elements.size() != 1 ){
			element = TextData.NewInstance(Feature.IMAGE());
			imageGallery.addElement(element);
		}else {
			element = elements.iterator().next();
		}
		String uriString = dbValue;
		Integer size = null;
		String mimeType = null;
		String suffix = null;
		URI uri = null;
		try {
			uri = new URI(uriString);
		} catch (URISyntaxException e) {
			String warning = "URISyntaxException when trying to convert first uri string: " + uriString;
			logger.error(warning);
		}
		Media media = Media.NewInstance(uri, size, mimeType, suffix);
		element.addMedia(media);
		return taxon;
	}

	//not used
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
    public Class<String> getTypeClass(){
		return String.class;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
	@Override
	public void initialize(DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass) {
		super.importMapperHelper.initialize(state, destinationClass);
	}


}
