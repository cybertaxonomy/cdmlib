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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * 
 * 
 * @author n.hoffmann
 * @created 18.11.2008
 * @version 1.0
 */
public class PalmaeImageActivator extends AbstractImageImporter {
	private static Logger logger = Logger.getLogger(PalmaeImageActivator.class);
	
	private static final File sourceFolder = new File("src/main/resources/images/palmae");
	private static final ICdmDataSource cdmDestination = CdmDestinations.cdm_preview_palmae();
	
	// set the webserver path to the images
	private static final String urlString = "http://wp5.e-taxonomy.eu/media/palmae/images/";
	
	static final UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");
	
	/**
	 * Rudimetary implementation using apache sanselan. This implementation depends
	 * on the metadata standards used in the palmae images. The IPTC field ObjectName
	 * contains a string like this: "Arecaceae; Eugeissona utilis". The string 
	 * in front of the semicolon is the family name and the one behind, the taxon name.
	 * So we basically assume, that if the string gets split by ";" the element at 
	 * index 1 should be the taxon name.
	 * If this format changes this method breaks!
	 * 
	 * TODO The ImageMetaData class of the commons package should provide 
	 * convenient access to the metadata of an image as well as all the error handling
	 * 
	 * @param imageFile
	 * @return the name of the taxon as stored in ObjectName IPTC tag
	 */
	public String retrieveTaxonNameFromImageMetadata(File imageFile){
		String name = null;
		
		IImageMetadata metadata = null;
		
		try {
			metadata = Sanselan.getMetadata(imageFile);
		} catch (ImageReadException e) {
			logger.error("Error reading image", e);
		} catch (IOException e) {
			logger.error("Error reading file", e);
		}
		
		if(metadata instanceof JpegImageMetadata){
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			for (Object object : jpegMetadata.getItems()){
				Item item = (Item) object;
				if(item.getKeyword().equals("ObjectName")){
					logger.warn("File: " + imageFile.getName() + ". ObjectName string is: " + item.getText());
					String[] objectNameSplit = item.getText().split(";");
					
					name = objectNameSplit[1];
				}				
			}
		}
		
		
		return name.trim();
	}
	
	protected boolean invokeImageImport (IImportConfigurator config){
		
		logger.info("importing images from directory: " + sourceFolder);
		
		if(sourceFolder.isDirectory()){
			for( File file : sourceFolder.listFiles()){
				if(file.isFile()){
				
					String taxonName = retrieveTaxonNameFromImageMetadata(file);
					logger.warn("Looking up taxa with taxon name: " + taxonName);
					List<TaxonBase> taxa = taxonService.searchTaxaByName(taxonName, config.getSourceReference());			
					
					
					if(taxa.size() == 0){
						logger.warn("no taxon with this name found" + taxonName);
					}else if(taxa.size() > 1){
						logger.error(taxa);
						logger.error("multiple taxa with this name found: " + taxonName);
					}else{
						Taxon taxon = (Taxon) taxa.get(0);
						
						taxonService.saveTaxon(taxon);
						
						TextData feature = TextData.NewInstance();
						
	
						ImageMetaData imageMetaData = new ImageMetaData();
						imageMetaData.readFrom(file);
						
						int width = imageMetaData.getWidth();
						int height = imageMetaData.getHeight();
						
						// TODO size is supposed to be be filesize
						int size = width * height;
						
						String mimeType = imageMetaData.getMimeType();
						String suffix = "jpg";
						
						
						// URL for this image
						URL url = null;
						try {
							url = new URL(urlString + file.getName());
						} catch (MalformedURLException e) {
							logger.warn("URL is malformed: "+ url);
						}
						
						
						ImageFile imageFile = ImageFile.NewInstance(url.toString(), null, imageMetaData);
						
						
						MediaRepresentation representation = MediaRepresentation.NewInstance(mimeType, suffix);
						representation.addRepresentationPart(imageFile);
						
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
