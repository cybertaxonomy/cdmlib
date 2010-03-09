// $Id$
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.mail.MethodNotSupportedException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This class maps a database attribute to CDM extension added to the target class
 * TODO maybe this class should not inherit from DbSingleAttributeImportMapperBase
 * as it does not map to a single attribute
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbImportImageGalleryMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, Taxon> implements IDbImportMapper<DbImportStateBase<?,?>,Taxon>{
	@SuppressWarnings("unused")
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
	public void initialize(ITermService service, BerlinModelImportState state, Class<? extends CdmBase> destinationClass) {
		importMapperHelper.initialize(state, destinationClass);
		try {
			if (  checkDbColumnExists()){
				//do nothing
			}else{
				ignore = true;
			}
		} catch (MethodNotSupportedException e) {
			//do nothing
		}
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public Taxon invoke(ResultSet rs, Taxon taxon) throws SQLException {
		String dbValue = rs.getString(getSourceAttribute());
		return invoke(dbValue, taxon);
	}
	
	/**
	 * @param dbValue
	 * @param identifiableEntity
	 * @return
	 */
	private Taxon invoke(String dbValue, Taxon taxon){
		if (ignore || CdmUtils.isEmpty(dbValue)){
			return taxon;
		}
		boolean createNew = true;
		TaxonDescription imageGallery = taxon.getImageGallery(createNew);
		Set<DescriptionElementBase> elements = imageGallery.getElements();
		DescriptionElementBase element = null;
		if (elements.size() != 1 ){
			element = TextData.NewInstance(Feature.IMAGE());
			imageGallery.addElement(element);
		}else {
			element = elements.iterator().next();
		}
		String uri = dbValue;
		Integer size = null;
		String mimeType = null;
		String suffix = null;
		Media media = Media.NewInstance(uri, size, mimeType, suffix);
		element.addMedia(media);
		return taxon;
	}
	
	//not used
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
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
