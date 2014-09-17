/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.algaterra;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @created 12.09.2012
 */
public abstract class AlgaTerraImageImportBase extends BerlinModelImportBase{
	private static final Logger logger = Logger.getLogger(AlgaTerraImageImportBase.class);

	public static final String TERMS_NAMESPACE = "ALGA_TERRA_TERMS";

	private static final String ALGAE_URL_BASE = "http://mediastorage.bgbm.org/fsi/server?type=image&profile=jpeg&quality=100&source=Algaterra%2FAlgae%2F";
	private static final String SITE_URL_BASE =  "http://mediastorage.bgbm.org/fsi/server?type=image&profile=jpeg&quality=100&source=Algaterra%2FSites%2F";
	private static final String VOUCHER_URL_BASE =  "http://mediastorage.bgbm.org/fsi/server?type=image&profile=jpeg&quality=100&source=Algaterra%2FVoucher%2F";

	private static final String ALGAE_URL_ORIGINAL = "http://media.bgbm.org/erez/erez?cmd=get&src=Algaterra%2FAlgae%2F";
	private static final String SITE_URL_ORIGINAL = "http://media.bgbm.org/erez/erez?cmd=get&src=Algaterra%2FSites%2F";
	private static final String VOUCHER_URL_ORIGINAL = "http://media.bgbm.org/erez/erez?cmd=get&src=Algaterra%2FVoucher%2F";
	
	
	protected enum PathType {
		Image (ALGAE_URL_BASE, ALGAE_URL_ORIGINAL),
		Site (SITE_URL_BASE, SITE_URL_ORIGINAL),
		Voucher (VOUCHER_URL_BASE, VOUCHER_URL_ORIGINAL)
		;
		
		String urlThumbnail;
		String urlOriginal;
		private PathType(String urlThumbnail, String urlOriginal){
			this.urlThumbnail = urlThumbnail;
			this.urlOriginal = urlOriginal;
		}
	}

	public AlgaTerraImageImportBase(String tableName, String pluralString) {
		super(tableName, pluralString);
	}

	
	/**
	 * Creates a media object and 
	 * @param rs
	 * @param derivedUnit
	 * @param state
	 * @param partitioner
	 * @return
	 * @throws SQLException
	 */
	protected Media handleSingleImage(ResultSet rs, IdentifiableEntity<?> identifiableEntity, AlgaTerraImportState state, ResultSetPartitioner partitioner, PathType pathType) throws SQLException {
		
		try {
			String figurePhrase = rs.getString("FigurePhrase");
//			String filePath = rs.getString("filePath");
			String fileName = rs.getString("fileName");
			//TODO  publishFlag
			Boolean publishFlag = rs.getBoolean("RestrictedFlag");
			
			
			if (isBlank(fileName)){
				throw new RuntimeException("FileName is missing");
			}
//			if (isBlank(filePath)){
//				filePath = state.getAlgaTerraConfigurator().getImageBaseUrl();
//			}
			
			
			//handle thumbnail
			String filePath = pathType.urlThumbnail;
			String fullPath = filePath + fileName;
			Media media = getImageMedia(fullPath, READ_MEDIA_DATA);
			if (media == null){
				logger.warn("Thumbnail image not found: " + filePath);
			}
			
			//handle original
			if (state.getAlgaTerraConfigurator().isImportOriginalSizeMedia()){
				filePath = pathType.urlOriginal;
				fullPath = filePath + fileName;
				Media mediaOriginal = getImageMedia(fullPath, READ_MEDIA_DATA);
				if (mediaOriginal != null){
					if (media == null){
						media = mediaOriginal;
					}else {
						media.addRepresentation(mediaOriginal.getRepresentations().iterator().next());
					}
				}else{
					logger.warn("Original image not found: " + filePath);
				}
			}
			
			if (media == null){
				throw new RuntimeException ("Media not found for " +fullPath);
			}
			
			
			
			if (isNotBlank(figurePhrase)){
				media.putTitle(Language.DEFAULT(), figurePhrase);
			}
			
			//TODO ref
			Reference<?> ref = null;
			if (identifiableEntity != null){
				DescriptionBase<?> desc = getDescription(identifiableEntity, ref, IMAGE_GALLERY, CREATE);
				TextData textData = null;
				for (DescriptionElementBase descEl : desc.getElements()){
					if (descEl.isInstanceOf(TextData.class)){
						textData = CdmBase.deproxy(descEl, TextData.class);
					}
				}
				if (textData == null){
					textData = TextData.NewInstance(Feature.IMAGE());
				}
				desc.addElement(textData);
				textData.addMedia(media);
			}else{
				logger.warn("Identifiable Entity is null. Can't add media ");
			}
			
			//notes
			
			//TODO restrictedFlag
			
			//TODO id, created for 
			//    	this.doIdCreatedUpdatedNotes(state, descriptionElement, rs, id, namespace);
		
			return media;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    	
	}




	private DescriptionBase<?> getDescription(IdentifiableEntity identifiableEntity, Reference<?> ref,
			boolean imageGallery, boolean create) {
		if (identifiableEntity.isInstanceOf(SpecimenOrObservationBase.class)){
			return getSpecimenDescription(CdmBase.deproxy(identifiableEntity, SpecimenOrObservationBase.class), ref, imageGallery, create);
		}else if (identifiableEntity.isInstanceOf(Taxon.class)){
			return getTaxonDescription(CdmBase.deproxy(identifiableEntity, Taxon.class), ref, imageGallery, create);
		}else{
			logger.warn("Unsupported IdentifiableEntity type: " +  identifiableEntity.getClass());
			return null;
		}
	}	
}
