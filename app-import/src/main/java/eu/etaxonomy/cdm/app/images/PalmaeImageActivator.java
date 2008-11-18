/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.images;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * TODO not working at the moment
 * 
 * @author n.hoffmann
 * @created 18.11.2008
 * @version 1.0
 */
public class PalmaeImageActivator extends AbstractImageImporter {
	private static Logger logger = Logger.getLogger(PalmaeImageActivator.class);
	
	private static final File sourceFolder = new File("src/main/resources/kew_images");
	private static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
	
	static final UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");

	/**
	 * TODO implement this using apache sanselan
	 * 
	 * @param imageFile
	 * @return
	 */
	private String retrieveTaxonNameFromImageMetadata(File imageFile){
		String name = null;
		
		
		
//		try {
////			Metadata metadata = JpegMetadataReader.readMetadata(imageFile);
//			
////			Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class); 
//			
////			String[] keywords = iptcDirectory.getStringArray(IptcDirectory.TAG_KEYWORDS);
////			
////			
////			for(String keyword : keywords){
////				logger.debug(keyword);
////			}
//			
////			String objectName = iptcDirectory.getString(IptcDirectory.TAG_OBJECT_NAME);
//			
////			String[] objectNameSplit = objectName.split(";");
////			for(String part : objectNameSplit){
////				logger.debug(part);
////			}
//			
//	//		name = objectNameSplit[1];
//			
//			
////		} catch (JpegProcessingException e1) {
////			logger.error(e1);
////		}
//		
		return name.trim();
	}
	
	protected boolean invokeImageImport (IImportConfigurator config){
		
		logger.info("importing images from directory: " + sourceFolder);
		
		if(sourceFolder.isDirectory()){
			for( File file : sourceFolder.listFiles()){
				logger.info(file);
				String taxonName = retrieveTaxonNameFromImageMetadata(file);
			
				List<TaxonBase> taxa = taxonService.searchTaxaByName(taxonName, config.getSourceReference());			
				
				
				if(taxa.size() == 0){
					logger.warn("no taxon with this name found" + taxonName);
				}else if(taxa.size() > 1){
					logger.warn("multiple taxa with this name found: " + taxonName);
				}else{
					Taxon taxon = (Taxon) taxa.get(0);
					
					taxonService.saveTaxon(taxon);
					
					TextData feature = TextData.NewInstance();
					
					URL url = null;
					try {
						url = new URL("test");
					} catch (MalformedURLException e) {
						logger.warn("URL is malformed: "+ url);
					}
					ImageMetaData imageMetaData = new ImageMetaData();
					imageMetaData.readFrom(url);
					
					int size = 100;
					
					String mimeType = "mime";
					String suffix = "suffix";
					
					
					
					MediaRepresentationPart mediaRepresentationPart = MediaRepresentationPart.NewInstance(url.toString(), size);
					
					MediaRepresentation representation = MediaRepresentation.NewInstance(mimeType, suffix);
					representation.addRepresentationPart(mediaRepresentationPart);
					
					Media media = Media.NewInstance();
					media.addRepresentation(representation);
					
					feature.addMedia(media);
					
					TaxonDescription description = TaxonDescription.NewInstance(taxon);
					
					description.addElement(feature);
					
					//taxon.addDescription(description);
					//taxonService.saveTaxon(taxon);
					//descriptionService.saveDescription(description);
					
				}
				
				logger.info(taxonName);
			}
		}else{
			logger.error("given source folder is not a directory");
		}
		return true;
	}
	
	public static void main (String[] cowabunga){
		ImageImportConfigurator imageConfigurator = ImageImportConfigurator.NewInstance(sourceFolder, cdmDestination);
		imageConfigurator.setSecUuid(secUuid);
		
		AbstractImageImporter imageImporter = new PalmaeImageActivator();
		imageImporter.invoke(imageConfigurator, null);
	}

}
