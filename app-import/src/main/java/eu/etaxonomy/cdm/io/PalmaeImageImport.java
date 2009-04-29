/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io;

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
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.app.images.AbstractImageImporter;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author n.hoffmann
 * @created 18.11.2008
 * @version 1.0
 */
@Component
public class PalmaeImageImport extends AbstractImageImporter {
	private static final Logger logger = Logger.getLogger(PalmaeImageImport.class);
	
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
					logger.info("File: " + imageFile.getName() + ". ObjectName string is: " + item.getText());
					String[] objectNameSplit = item.getText().split(";");
					
					name = objectNameSplit[1];
				}				
			}
		}
		
		
		return name.trim();
	}

	protected boolean invokeImageImport (ImageImportConfigurator config){
		
		logger.info("Importing images from directory: " + config.getSourceNameString());
		File sourceFolder = (File)config.getSource();
		if(sourceFolder.isDirectory()){
			for( File file : sourceFolder.listFiles()){
				if(file.isFile()){
				
					String taxonName = retrieveTaxonNameFromImageMetadata(file);
					logger.info("Looking up taxa with taxon name: " + taxonName);
					
					ReferenceBase sec = referenceService.getReferenceByUuid(config.getSecUuid());

					List<TaxonBase> taxa = taxonService.searchTaxaByName(taxonName, sec);			
					
					if(taxa.size() == 0){
						logger.warn("no taxon with this name found: " + taxonName);
					}else if(taxa.size() > 1){
						logger.error(taxa);
						logger.error("multiple taxa with this name found: " + taxonName);
					}else{
						Taxon taxon = (Taxon) taxa.get(0);
						
						taxonService.saveTaxon(taxon);
						
						TextData descriptionElement = TextData.NewInstance();
	
						ImageMetaData imageMetaData = new ImageMetaData();
						imageMetaData.readFrom(file);
						
						String mimeType = imageMetaData.getMimeType();
						String suffix = "jpg";
						
						
						// URL for this image
						URL url = null;
						try {
							url = new URL(config.getMediaUrlString() + file.getName());
						} catch (MalformedURLException e) {
							logger.warn("URL is malformed: "+ url);
						}
						
						
						ImageFile imageFile = ImageFile.NewInstance(url.toString(), null, imageMetaData);
						
						
						MediaRepresentation representation = MediaRepresentation.NewInstance(mimeType, suffix);
						representation.addRepresentationPart(imageFile);
						
						Media media = Media.NewInstance();
						media.addRepresentation(representation);
						
						descriptionElement.addMedia(media);
						descriptionElement.putText(taxonName, Language.ENGLISH());
						//descriptionElement.setFeature(Feature.IMAGE());
						descriptionElement.setType(Feature.IMAGE());
						
						TaxonDescription description = TaxonDescription.NewInstance(taxon);
						
						description.addElement(descriptionElement);
						taxon.addDescription(description);
						
						taxonService.saveTaxon(taxon);
						
						//descriptionService.saveDescription(description);
						
					}
				}
			}
		}else{
			logger.error("given source folder is not a directory");
		}
		
		return true;
	}
	
}
