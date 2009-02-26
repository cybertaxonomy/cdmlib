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
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.app.images.AbstractImageImporter;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author n.hoffmann
 * @created 18.11.2008
 * @version 1.0
 */
@Component
public class CichorieaeImageImport extends AbstractImageImporter {
	private static final Logger logger = Logger.getLogger(CichorieaeImageImport.class);
	
	private static final String URL = "URL";
	private static final String URL_APP = "URL_APP";
	private static final String NAME = "NAME";
	//private static final String CODE = "CODE";
	
	protected boolean invokeImageImport_ (ImageImportConfigurator config){
		
		ArrayList<HashMap<String, String>> contents;
		try {
			contents = ExcelUtils.parseXLS(config.getSource().toString());
		} catch (FileNotFoundException e1) {
			logger.error("FileNotFound: " + config.getSource().toString());
			return false;
		}
		
		for (HashMap<String, String> row : contents){
			
			String taxonName = row.get(CichorieaeImageImport.NAME).trim();
			
			INameService nameService = getNameService();
			List<TaxonBase> taxa = taxonService.searchTaxaByName(taxonName, config.getSourceReference());			
			
			if(taxa.size() == 0){
				logger.warn("no taxon with this name found: " + taxonName);
			}else if(taxa.size() > 1){
				logger.warn("multiple taxa with this name found: " + taxonName);
			}else{
				Taxon taxon = (Taxon) taxa.get(0);
				
				taxonService.saveTaxon(taxon);
				
				TextData feature = TextData.NewInstance();
				
				logger.info("Importing image for taxon: " + taxa);
				
				
				ImageMetaData imageMetaData = new ImageMetaData();
				
				
				try {
					URL url = new URL(row.get(CichorieaeImageImport.URL).trim());
					
					imageMetaData.readFrom(url);
					
					ImageFile image = ImageFile.NewInstance(url.toString(), null, imageMetaData);
					
					MediaRepresentation representation = MediaRepresentation.NewInstance(imageMetaData.getMimeType(), null);
					representation.addRepresentationPart(image);
					
					Media media = Media.NewInstance();
					media.addRepresentation(representation);
					
					feature.addMedia(media);
					//feature.putText(row.get(CichorieaeImageImport.URL_APP).trim(), Language.ENGLISH());
					
					feature.setType(Feature.IMAGE());
					
					TaxonDescription description = TaxonDescription.NewInstance(taxon);
					
					description.addElement(feature);
					
				} catch (MalformedURLException e) {
					logger.error("Malformed URL", e);
				}
				
			}
		}
		return true;
		
	}
	
	protected boolean invokeImageImport (ImageImportConfigurator config){
		File source = (File)config.getSource();
		if (source.isDirectory()){
			for (File file : source.listFiles() ){
				if (file.isFile()){
					String name = file.getName();
					String[] fileNameParts = name.split("\\.");
					if (fileNameParts.length < 2){
						logger.warn("No file extension found for: " +  name);
						continue;
					}
					String extension = fileNameParts[fileNameParts.length - 1];
					if (! "jpg".equalsIgnoreCase(extension)) { 
						logger.warn("Extension not recognized: " + extension);
						// Sometimes occurs here "Thumbs.db"
						continue;
					}
					String firstPart = name.substring(0, name.length() - extension.length() - 1);
					logger.info(firstPart);
					String[] nameParts = firstPart.split("_");
					if (nameParts.length < 3){
						logger.warn("name string has less than 2 '_'");
						continue;
					}
					String featureString = nameParts[nameParts.length-2];
					logger.debug("FeatureString: " +  featureString);
					String detailString = nameParts[nameParts.length-1];
					logger.debug("detailString: " +  detailString);
				
					String taxonName = "";
					for (int i= 0; i < nameParts.length-2; i++){
						taxonName += nameParts[i] + " ";
					}
					taxonName = taxonName.trim();
					logger.info("Taxon name: " +  taxonName);
					
					String _s_ = " s ";
					String subsp = " subsp. ";
					if (taxonName.contains(_s_)) {
						taxonName = taxonName.replace(_s_, subsp);
						logger.info("Taxon name: " +  taxonName);
					}
					
					List<TaxonBase> taxa = taxonService.searchTaxaByName(taxonName, config.getSourceReference());			
					
					if(taxa.size() == 0){
						logger.warn("no taxon with this name found: " + taxonName);
					}else if(taxa.size() > 1){
						logger.warn("multiple taxa with this name found: " + taxonName);
					}else{
						Taxon taxon = (Taxon) taxa.get(0);
						taxonService.saveTaxon(taxon);
						TextData feature = TextData.NewInstance();
						logger.info("Importing image for taxon: " + taxa);
						ImageMetaData imageMetaData = new ImageMetaData();
						try {
							String urlPrefix = "http://media.bgbm.org/erez/erez?src=EditWP6/photos/";
							String urlString = urlPrefix + name;
							logger.info(urlString);
							URL url = new URL(urlString);
							
							imageMetaData.readFrom(url);
							ImageFile image = ImageFile.NewInstance(url.toString(), null, imageMetaData);
							MediaRepresentation representation = MediaRepresentation.NewInstance(imageMetaData.getMimeType(), null);
							representation.addRepresentationPart(image);
							Media media = Media.NewInstance();
							media.addRepresentation(representation);
							feature.addMedia(media);
							feature.putText(taxonName, Language.ENGLISH());
							
							feature.setType(Feature.IMAGE());
							TaxonDescription description = TaxonDescription.NewInstance(taxon);
							description.addElement(feature);
							
						} catch (MalformedURLException e) {
							logger.error("Malformed URL", e);
						}
					}				
				}else{
					logger.warn("File is not a file (but a directory?): " + file.getName());
				}
			}	
		}else{
			logger.warn("Source is not a directory!" + source.toString());
		}
	
		return true;
		
	}
}
